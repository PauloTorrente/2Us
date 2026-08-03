package com.coupleapp.service.impl;

import com.coupleapp.entity.Notification.NotificationType;
import com.coupleapp.entity.User;
import com.coupleapp.entity.WishlistItem;
import com.coupleapp.repository.UserRepository;
import com.coupleapp.repository.WishlistItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

// Daily re-check of every wishlist item that came from a product link: if the price dropped
// since it was saved, updates it and notifies both partners. This is the mechanism behind "avise
// quando tiver promoção" — a local cache on the phone has no way to detect anything on its own
// without the app running, so this rides the same scheduled-job + notification engine already
// used for calendar reminders (see ReminderScheduler).
@Component
@RequiredArgsConstructor
@Slf4j
public class WishlistPriceScheduler {

    private final WishlistItemRepository wishlistItemRepository;
    private final UserRepository userRepository;
    private final LinkUnfurlService linkUnfurlService;
    private final NotificationService notificationService;

    // Runs daily at 09:00, an hour after ReminderScheduler, so they don't compete for outbound
    // HTTP/DB resources at the exact same moment.
    @Scheduled(cron = "0 0 9 * * *")
    @Transactional
    public void checkPriceDrops() {
        List<WishlistItem> items = wishlistItemRepository
                .findBySourceAndIsFulfilledFalseAndProductUrlIsNotNull(WishlistItem.WishlistSource.LINK);

        int checked = 0;
        int drops = 0;
        for (WishlistItem item : items) {
            checked++;
            BigDecimal newPrice = linkUnfurlService.unfurl(item.getProductUrl()).price();
            if (newPrice == null) continue;

            BigDecimal oldPrice = item.getPrice();
            if (oldPrice != null && newPrice.compareTo(oldPrice) < 0) {
                item.setPrice(newPrice);
                wishlistItemRepository.save(item);
                notifyCouple(item, oldPrice, newPrice);
                drops++;
            } else if (oldPrice == null) {
                // First time we manage to read a price for this item — just record it, no
                // "drop" to announce yet since there was nothing to compare against.
                item.setPrice(newPrice);
                wishlistItemRepository.save(item);
            }
        }

        log.info("WishlistPriceScheduler: checked {} items, found {} price drops", checked, drops);
    }

    private void notifyCouple(WishlistItem item, BigDecimal oldPrice, BigDecimal newPrice) {
        List<User> partners = userRepository.findByCoupleId(item.getCouple().getId());
        String message = String.format("Baixou de preço! \"%s\" caiu de R$ %.2f para R$ %.2f",
                item.getTitle(), oldPrice, newPrice);
        for (User partner : partners) {
            notificationService.create(partner, message, NotificationType.WISHLIST_UPDATE, item.getId());
        }
    }
}

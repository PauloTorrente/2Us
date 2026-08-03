package com.coupleapp.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Best-effort product page "unfurl": pulls title/image/price from Open Graph tags when a
// partner pastes a product link (Amazon, Shopee, Mercado Livre, Steam, etc). Amazon in
// particular blocks server-side fetches often (bot detection) — when a page can't be read this
// just returns an empty result and the app falls back to manual entry, it never throws.
@Service
@Slf4j
public class LinkUnfurlService {

    private static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0 Safari/537.36";
    private static final Pattern PRICE_PATTERN = Pattern.compile("R\\$\\s?([\\d.,]+)");

    public record UnfurlResult(String title, String imageUrl, BigDecimal price) {}

    public UnfurlResult unfurl(String url) {
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent(USER_AGENT)
                    .timeout(6000)
                    .followRedirects(true)
                    .get();

            String title = firstNonBlank(metaContent(doc, "og:title"), doc.title());
            String imageUrl = metaContent(doc, "og:image");
            BigDecimal price = extractPrice(doc);

            return new UnfurlResult(title, imageUrl, price);
        } catch (Exception e) {
            log.warn("LinkUnfurlService: failed to unfurl {} ({})", url, e.getMessage());
            return new UnfurlResult(null, null, null);
        }
    }

    private String metaContent(Document doc, String property) {
        var el = doc.selectFirst("meta[property=" + property + "]");
        return el != null ? el.attr("content") : null;
    }

    // Tries the structured og:price tag first; falls back to the first "R$ ..." pattern found
    // in the page text. The fallback is noisy by nature (could match shipping/installments) —
    // it's a best-effort preview the user can always correct before saving.
    private BigDecimal extractPrice(Document doc) {
        String ogPrice = metaContent(doc, "product:price:amount");
        if (ogPrice != null && !ogPrice.isBlank()) {
            try {
                return new BigDecimal(ogPrice.replace(",", "."));
            } catch (NumberFormatException ignored) {
            }
        }

        Matcher matcher = PRICE_PATTERN.matcher(doc.text());
        if (matcher.find()) {
            String raw = matcher.group(1).replace(".", "").replace(",", ".");
            try {
                return new BigDecimal(raw);
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

    private String firstNonBlank(String... values) {
        for (String v : values) {
            if (v != null && !v.isBlank()) return v;
        }
        return null;
    }
}

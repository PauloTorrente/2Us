-- Baseline schema, generated from the JPA entities as they stood when Flyway was introduced.
-- On the existing prod DB this is skipped (baseline-on-migrate); fresh DBs run it as V1.

    create table activity_logs (
        activity_id bigint not null,
        completed_at timestamp(6) not null,
        created_at timestamp(6),
        id bigserial not null,
        logged_by_user_id bigint not null,
        note varchar(300),
        primary key (id)
    );
    create table availability_windows (
        end_date date not null,
        start_date date not null,
        couple_id bigint not null,
        created_at timestamp(6),
        id bigserial not null,
        user_id bigint not null,
        type varchar(20) not null check (type in ('DAY_OFF','VACATION','FLEXIBLE')),
        label varchar(200),
        primary key (id)
    );
    create table calendar_events (
        end_date date,
        event_date date not null,
        recurring_yearly boolean,
        reminder_days_before integer,
        couple_id bigint not null,
        created_at timestamp(6),
        created_by_user_id bigint,
        id bigserial not null,
        event_type varchar(30) not null check (event_type in ('ANNIVERSARY','BIRTHDAY','MENSTRUAL_CYCLE','VACATION','APPOINTMENT','CUSTOM')),
        title varchar(150) not null,
        description varchar(500),
        primary key (id)
    );
    create table couples (
        anniversary_date date,
        exact_date_known boolean,
        relationship_start_date date,
        invite_code varchar(6) not null unique,
        created_at timestamp(6) not null,
        id bigserial not null,
        couple_name varchar(255),
        status varchar(255) not null check (status in ('PENDING_INVITE','PENDING','ACTIVE','PAUSED','ENDED')),
        primary key (id)
    );
    create table finance_goals (
        current_amount numeric(12,2) not null,
        target_amount numeric(12,2) not null,
        target_date date,
        couple_id bigint not null,
        created_at timestamp(6),
        id bigserial not null,
        updated_at timestamp(6),
        horizon varchar(10) not null check (horizon in ('SHORT','MEDIUM','LONG')),
        status varchar(15) not null check (status in ('IN_PROGRESS','ACHIEVED','ABANDONED')),
        title varchar(150) not null,
        description varchar(500),
        primary key (id)
    );
    create table finance_transactions (
        amount numeric(12,2) not null,
        is_recurring boolean,
        transaction_date date not null,
        couple_id bigint not null,
        created_at timestamp(6),
        id bigserial not null,
        recorded_by_user_id bigint not null,
        type varchar(10) not null check (type in ('INCOME','EXPENSE')),
        category varchar(20) not null check (category in ('HOUSING','FOOD','TRANSPORT','HEALTH','LEISURE','SUBSCRIPTIONS','CLOTHING','EDUCATION','SAVINGS','OTHER')),
        description varchar(150) not null,
        note varchar(300),
        primary key (id)
    );
    create table notifications (
        read boolean not null,
        created_at timestamp(6),
        id bigserial not null,
        recipient_user_id bigint not null,
        reference_id bigint,
        scheduled_for timestamp(6),
        type varchar(30) not null check (type in ('TASK_ASSIGNED','TASK_DUE_SOON','TASK_OVERDUE','TASK_COMPLETED','CALENDAR_REMINDER','ANNIVERSARY_REMINDER','COUPLE_INVITE','WISHLIST_UPDATE','FINANCE_ALERT','ACTIVITY_REMINDER','GOAL_MILESTONE')),
        message varchar(300) not null,
        primary key (id)
    );
    create table shared_activities (
        completion_count integer,
        is_active boolean,
        couple_id bigint not null,
        created_at timestamp(6),
        id bigserial not null,
        updated_at timestamp(6),
        category varchar(20) not null check (category in ('FITNESS','LEARNING','LEISURE','HEALTH','OTHER')),
        frequency varchar(20) not null check (frequency in ('DAILY','WEEKLY','BIWEEKLY','MONTHLY')),
        name varchar(100) not null,
        description varchar(300),
        primary key (id)
    );
    create table suggestion_preferences (
        max_distance_meters integer,
        min_rating float(53),
        prefer_local boolean,
        price_level integer,
        couple_id bigint not null unique,
        created_at timestamp(6),
        id bigserial not null,
        updated_at timestamp(6),
        business_types varchar(500),
        cuisine_types varchar(500),
        primary key (id)
    );
    create table tasks (
        assigned_to bigint,
        completed_at timestamp(6),
        couple_id bigint not null,
        created_at timestamp(6) not null,
        id bigserial not null,
        assignment varchar(255) not null check (assignment in ('RANDOM','AGREED','MANUAL')),
        description varchar(255) not null,
        status varchar(255) not null check (status in ('PENDING','IN_PROGRESS','COMPLETED')),
        primary key (id)
    );
    create table users (
        couple_id bigint,
        created_at timestamp(6) not null,
        id bigserial not null,
        email varchar(255) not null unique,
        name varchar(255) not null,
        password varchar(255) not null,
        primary key (id)
    );
    create table wishlist_items (
        is_fulfilled boolean,
        price numeric(10,2),
        couple_id bigint not null,
        created_at timestamp(6),
        id bigserial not null,
        user_id bigint not null,
        priority varchar(10) not null check (priority in ('LOW','MEDIUM','HIGH')),
        source varchar(20) check (source in ('MANUAL','LINK','IMAGE')),
        title varchar(200) not null,
        image_url varchar(500),
        note varchar(500),
        product_url varchar(500),
        primary key (id)
    );
    alter table if exists activity_logs 
       add constraint FK33afi1s6yk8ixk53tuxo4tgx1 
       foreign key (activity_id) 
       references shared_activities;
    alter table if exists activity_logs 
       add constraint FKlcchphhy3kjdss4yoy3a0vm77 
       foreign key (logged_by_user_id) 
       references users;
    alter table if exists availability_windows 
       add constraint FKxqpt3dp4ricwxjayfm5ilvbj 
       foreign key (couple_id) 
       references couples;
    alter table if exists availability_windows 
       add constraint FKnf7gcmcjw7ifqd1ni66wcx8kj 
       foreign key (user_id) 
       references users;
    alter table if exists calendar_events 
       add constraint FKfam10swx3qa5ymiybc1y86dcw 
       foreign key (couple_id) 
       references couples;
    alter table if exists calendar_events 
       add constraint FK8yiot4nxh1vnkobq3kvc7j5xc 
       foreign key (created_by_user_id) 
       references users;
    alter table if exists finance_goals 
       add constraint FKk85s2ngrpdtaesg924ghsdy5d 
       foreign key (couple_id) 
       references couples;
    alter table if exists finance_transactions 
       add constraint FKpocif086ed6kxmtc6i4nnekq5 
       foreign key (couple_id) 
       references couples;
    alter table if exists finance_transactions 
       add constraint FKr44u2vbatf1mpu6vwwxty59df 
       foreign key (recorded_by_user_id) 
       references users;
    alter table if exists notifications 
       add constraint FKt8ievafor22iuvg5sd4p7lhbk 
       foreign key (recipient_user_id) 
       references users;
    alter table if exists shared_activities 
       add constraint FK61lqtutcc6uxdmkv3ok5fo2iy 
       foreign key (couple_id) 
       references couples;
    alter table if exists suggestion_preferences 
       add constraint FK3xa1xbfrt0ajhjxphsfvedhyu 
       foreign key (couple_id) 
       references couples;
    alter table if exists tasks 
       add constraint FK2vjo8mbre3rvpbd6e7976b54m 
       foreign key (assigned_to) 
       references users;
    alter table if exists tasks 
       add constraint FKbr8ia8y8q9vcmhkt9oybs5sk6 
       foreign key (couple_id) 
       references couples;
    alter table if exists users 
       add constraint FKmqanndnveg6o7es6wophwbovc 
       foreign key (couple_id) 
       references couples;
    alter table if exists wishlist_items 
       add constraint FKjq7vcw4e0me217ltkrxuow4f9 
       foreign key (couple_id) 
       references couples;
    alter table if exists wishlist_items 
       add constraint FKmmj2k1i459yu449k3h1vx5abp 
       foreign key (user_id) 
       references users;

package com.vuthevy1209.springmail.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;


@Getter
@RequiredArgsConstructor
public enum MailLabel {
    // 1. System Labels (Common)
    INBOX("INBOX", "Inbox", LabelType.SYSTEM, "Email in the inbox"),
    SENT("SENT", "Sent", LabelType.SYSTEM, "Sent messages"),
    DRAFT("DRAFT", "Draft", LabelType.SYSTEM, "Draft messages"),
    TRASH("TRASH", "Trash", LabelType.SYSTEM, "Messages in trash"),
    SPAM("SPAM", "Spam", LabelType.SYSTEM, "Spam messages"),
    STARRED("STARRED", "Starred", LabelType.SYSTEM, "Starred messages"),
    UNREAD("UNREAD", "Unread", LabelType.SYSTEM, "Unread messages"),
    IMPORTANT("IMPORTANT", "Important", LabelType.SYSTEM, "Important messages"),

    // 2. Automatic Classification Labels (Category Labels)
    CATEGORY_PERSONAL("CATEGORY_PERSONAL", "Personal", LabelType.CATEGORY, "Personal emails"),
    CATEGORY_SOCIAL("CATEGORY_SOCIAL", "Social", LabelType.CATEGORY, "Social network notifications"),
    CATEGORY_PROMOTIONS("CATEGORY_PROMOTIONS", "Promotions", LabelType.CATEGORY, "Marketing and promotions"),
    CATEGORY_UPDATES("CATEGORY_UPDATES", "Updates", LabelType.CATEGORY, "Automatic updates and confirmations"),
    CATEGORY_FORUMS("CATEGORY_FORUMS", "Forums", LabelType.CATEGORY, "Messages from forums and groups");

    private final String id;
    private final String displayName;
    private final LabelType type;
    private final String description;


    public enum LabelType {
        SYSTEM, CATEGORY
    }

    public static MailLabel fromId(String id) {
        if (id == null) return null;
        for (MailLabel label : values()) {
            if (label.id.equalsIgnoreCase(id)) {
                return label;
            }
        }
        return null;
    }
}

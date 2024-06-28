/*
 * Copyright (c) 2020 Dennis Lang (LanDen Labs) landenlabs@gmail.com
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the
 * following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN
 * NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * @author Dennis Lang
 * @see https://LanDenLabs.com/
 */

package com.landenlabs.all_devtool;

/**
 * Display Icon assets in the android.R.drawable section.
 *
 * @author Dennis Lang
 */
public class IconDrawFragment extends IconBaseFragment {

    public static final String s_name = "IconDraw";

    public IconDrawFragment() {
    }

    public static IconDrawFragment create() {
        return new IconDrawFragment();
    }

    // ============================================================================================
    // IconBaseFragment methods

    @Override
    public String getName() {
        return s_name;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void addToList() {

        addIcon("ic_btn_speak_now", android.R.drawable.ic_btn_speak_now);
        addIcon("ic_delete", android.R.drawable.ic_delete);
        addIcon("ic_dialog_alert", android.R.drawable.ic_dialog_alert);
        addIcon("ic_dialog_dialer", android.R.drawable.ic_dialog_dialer);
        addIcon("ic_dialog_email", android.R.drawable.ic_dialog_email);
        addIcon("ic_dialog_info", android.R.drawable.ic_dialog_info);
        addIcon("ic_dialog_map", android.R.drawable.ic_dialog_map);
        addIcon("ic_input_add", android.R.drawable.ic_input_add);
        addIcon("ic_input_delete", android.R.drawable.ic_input_delete);
        addIcon("ic_input_get", android.R.drawable.ic_input_get);
        addIcon("ic_lock_idle_alarm", android.R.drawable.ic_lock_idle_alarm);
        addIcon("ic_lock_idle_charging",
                android.R.drawable.ic_lock_idle_charging);
        addIcon("ic_lock_idle_lock", android.R.drawable.ic_lock_idle_lock);
        addIcon("ic_lock_idle_low_battery",
                android.R.drawable.ic_lock_idle_low_battery);
        addIcon("ic_lock_lock", android.R.drawable.ic_lock_lock);
        addIcon("ic_lock_power_off", android.R.drawable.ic_lock_power_off);
        addIcon("ic_lock_silent_mode", android.R.drawable.ic_lock_silent_mode);
        addIcon("ic_lock_silent_mode_off",
                android.R.drawable.ic_lock_silent_mode_off);
        addIcon("ic_media_ff", android.R.drawable.ic_media_ff);
        addIcon("ic_media_next", android.R.drawable.ic_media_next);
        addIcon("ic_media_pause", android.R.drawable.ic_media_pause);
        addIcon("ic_media_play", android.R.drawable.ic_media_play);
        addIcon("ic_media_previous", android.R.drawable.ic_media_previous);
        addIcon("ic_media_rew", android.R.drawable.ic_media_rew);
        addIcon("ic_menu_add", android.R.drawable.ic_menu_add);
        addIcon("ic_menu_agenda", android.R.drawable.ic_menu_agenda);
        addIcon("ic_menu_always_landscape_portrait",
                android.R.drawable.ic_menu_always_landscape_portrait);
        addIcon("ic_menu_call", android.R.drawable.ic_menu_call);
        addIcon("ic_menu_camera", android.R.drawable.ic_menu_camera);
        addIcon("ic_menu_close_clear_cancel",
                android.R.drawable.ic_menu_close_clear_cancel);
        addIcon("ic_menu_compass", android.R.drawable.ic_menu_compass);
        addIcon("ic_menu_crop", android.R.drawable.ic_menu_crop);
        addIcon("ic_menu_day", android.R.drawable.ic_menu_day);
        addIcon("ic_menu_delete", android.R.drawable.ic_menu_delete);
        addIcon("ic_menu_directions", android.R.drawable.ic_menu_directions);
        addIcon("ic_menu_edit", android.R.drawable.ic_menu_edit);
        addIcon("ic_menu_gallery", android.R.drawable.ic_menu_gallery);
        addIcon("ic_menu_help", android.R.drawable.ic_menu_help);
        addIcon("ic_menu_info_details", android.R.drawable.ic_menu_info_details);
        addIcon("ic_menu_manage", android.R.drawable.ic_menu_manage);
        addIcon("ic_menu_mapmode", android.R.drawable.ic_menu_mapmode);
        addIcon("ic_menu_month", android.R.drawable.ic_menu_month);
        addIcon("ic_menu_more", android.R.drawable.ic_menu_more);
        addIcon("ic_menu_my_calendar", android.R.drawable.ic_menu_my_calendar);
        addIcon("ic_menu_mylocation", android.R.drawable.ic_menu_mylocation);
        addIcon("ic_menu_myplaces", android.R.drawable.ic_menu_myplaces);
        addIcon("ic_menu_preferences", android.R.drawable.ic_menu_preferences);
        addIcon("ic_menu_recent_history",
                android.R.drawable.ic_menu_recent_history);
        addIcon("ic_menu_report_image", android.R.drawable.ic_menu_report_image);
        addIcon("ic_menu_revert", android.R.drawable.ic_menu_revert);
        addIcon("ic_menu_rotate", android.R.drawable.ic_menu_rotate);
        addIcon("ic_menu_save", android.R.drawable.ic_menu_save);
        addIcon("ic_menu_search", android.R.drawable.ic_menu_search);
        addIcon("ic_menu_send", android.R.drawable.ic_menu_send);
        addIcon("ic_menu_set_as", android.R.drawable.ic_menu_set_as);
        addIcon("ic_menu_share", android.R.drawable.ic_menu_share);
        addIcon("ic_menu_slideshow", android.R.drawable.ic_menu_slideshow);
        addIcon("ic_menu_sort_alphabetically",
                android.R.drawable.ic_menu_sort_alphabetically);
        addIcon("ic_menu_sort_by_size", android.R.drawable.ic_menu_sort_by_size);
        addIcon("ic_menu_today", android.R.drawable.ic_menu_today);
        addIcon("ic_menu_upload", android.R.drawable.ic_menu_upload);
        addIcon("ic_menu_upload_you_tube",
                android.R.drawable.ic_menu_upload_you_tube);
        addIcon("ic_menu_view", android.R.drawable.ic_menu_view);
        addIcon("ic_menu_week", android.R.drawable.ic_menu_week);
        addIcon("ic_menu_zoom", android.R.drawable.ic_menu_zoom);
        addIcon("ic_notification_clear_all",
                android.R.drawable.ic_notification_clear_all);
        addIcon("ic_notification_overlay",
                android.R.drawable.ic_notification_overlay);
        addIcon("ic_partial_secure", android.R.drawable.ic_partial_secure);
        addIcon("ic_popup_disk_full", android.R.drawable.ic_popup_disk_full);
        addIcon("ic_popup_reminder", android.R.drawable.ic_popup_reminder);
        addIcon("ic_popup_sync", android.R.drawable.ic_popup_sync);
        addIcon("ic_search_category_default",
                android.R.drawable.ic_search_category_default);
        addIcon("ic_secure", android.R.drawable.ic_secure);
        addIcon("list_selector_background",
                android.R.drawable.list_selector_background);
        addIcon("menu_frame", android.R.drawable.menu_frame);
        addIcon("menu_full_frame", android.R.drawable.menu_full_frame);
        addIcon("menuitem_background", android.R.drawable.menuitem_background);
        addIcon("picture_frame", android.R.drawable.picture_frame);
        addIcon("presence_audio_away", android.R.drawable.presence_audio_away);
        addIcon("presence_audio_busy", android.R.drawable.presence_audio_busy);
        addIcon("presence_audio_online",
                android.R.drawable.presence_audio_online);
        addIcon("presence_away", android.R.drawable.presence_away);
        addIcon("presence_busy", android.R.drawable.presence_busy);
        addIcon("presence_invisible", android.R.drawable.presence_invisible);
        addIcon("presence_offline", android.R.drawable.presence_offline);
        addIcon("presence_online", android.R.drawable.presence_online);
        addIcon("presence_video_away", android.R.drawable.presence_video_away);
        addIcon("presence_video_busy", android.R.drawable.presence_video_busy);
        addIcon("presence_video_online",
                android.R.drawable.presence_video_online);
        addIcon("progress_horizontal", android.R.drawable.progress_horizontal);
        addIcon("progress_indeterminate_horizontal",
                android.R.drawable.progress_indeterminate_horizontal);
        addIcon("radiobutton_off_background",
                android.R.drawable.radiobutton_off_background);
        addIcon("radiobutton_on_background",
                android.R.drawable.radiobutton_on_background);
        addIcon("screen_background_dark",
                android.R.drawable.screen_background_dark);
        addIcon("screen_background_dark_transparent",
                android.R.drawable.screen_background_dark_transparent);
        addIcon("screen_background_light",
                android.R.drawable.screen_background_light);
        addIcon("screen_background_light_transparent",
                android.R.drawable.screen_background_light_transparent);
        addIcon("spinner_background", android.R.drawable.spinner_background);
        addIcon("spinner_dropdown_background",
                android.R.drawable.spinner_dropdown_background);
        addIcon("star_big_off", android.R.drawable.star_big_off);
        addIcon("star_big_on", android.R.drawable.star_big_on);
        addIcon("star_off", android.R.drawable.star_off);
        addIcon("star_on", android.R.drawable.star_on);
        addIcon("stat_notify_call_mute",
                android.R.drawable.stat_notify_call_mute);
        addIcon("stat_notify_chat", android.R.drawable.stat_notify_chat);
        addIcon("stat_notify_error", android.R.drawable.stat_notify_error);
        addIcon("stat_notify_missed_call",
                android.R.drawable.stat_notify_missed_call);
        addIcon("stat_notify_more", android.R.drawable.stat_notify_more);
        addIcon("stat_notify_sdcard", android.R.drawable.stat_notify_sdcard);
        addIcon("stat_notify_sdcard_prepare",
                android.R.drawable.stat_notify_sdcard_prepare);
        addIcon("stat_notify_sdcard_usb",
                android.R.drawable.stat_notify_sdcard_usb);
        addIcon("stat_notify_sync", android.R.drawable.stat_notify_sync);
        addIcon("stat_notify_sync_noanim",
                android.R.drawable.stat_notify_sync_noanim);
        addIcon("stat_notify_voicemail",
                android.R.drawable.stat_notify_voicemail);
        addIcon("stat_sys_data_bluetooth",
                android.R.drawable.stat_sys_data_bluetooth);
        addIcon("stat_sys_download", android.R.drawable.stat_sys_download);
        addIcon("stat_sys_download_done",
                android.R.drawable.stat_sys_download_done);
        addIcon("stat_sys_headset", android.R.drawable.stat_sys_headset);
        addIcon("stat_sys_phone_call", android.R.drawable.stat_sys_phone_call);
        addIcon("stat_sys_phone_call_forward",
                android.R.drawable.stat_sys_phone_call_forward);
        addIcon("stat_sys_phone_call_on_hold",
                android.R.drawable.stat_sys_phone_call_on_hold);
        addIcon("stat_sys_speakerphone",
                android.R.drawable.stat_sys_speakerphone);
        addIcon("stat_sys_upload", android.R.drawable.stat_sys_upload);
        addIcon("stat_sys_upload_done", android.R.drawable.stat_sys_upload_done);
        addIcon("stat_sys_vp_phone_call",
                android.R.drawable.stat_sys_vp_phone_call);
        addIcon("stat_sys_vp_phone_call_on_hold",
                android.R.drawable.stat_sys_vp_phone_call_on_hold);
        addIcon("stat_sys_warning", android.R.drawable.stat_sys_warning);
        addIcon("status_bar_item_app_background",
                android.R.drawable.status_bar_item_app_background);
        addIcon("status_bar_item_background",
                android.R.drawable.status_bar_item_background);
        addIcon("sym_action_call", android.R.drawable.sym_action_call);
        addIcon("sym_action_chat", android.R.drawable.sym_action_chat);
        addIcon("sym_action_email", android.R.drawable.sym_action_email);
        addIcon("sym_call_incoming", android.R.drawable.sym_call_incoming);
        addIcon("sym_call_missed", android.R.drawable.sym_call_missed);
        addIcon("sym_call_outgoing", android.R.drawable.sym_call_outgoing);
        addIcon("sym_contact_card", android.R.drawable.sym_contact_card);
        addIcon("sym_def_app_icon", android.R.drawable.sym_def_app_icon);
        addIcon("title_bar", android.R.drawable.title_bar);
        addIcon("title_bar_tall", android.R.drawable.title_bar_tall);
        addIcon("toast_frame", android.R.drawable.toast_frame);
        addIcon("zoom_plate", android.R.drawable.zoom_plate);

    }

}
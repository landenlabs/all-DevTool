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
 * @see http://LanDenLabs.com/
 */

package com.landenlabs.all_devtool;

/**
 * Display Numeric assets in the android.R.dimen section.
 *
 * @author Dennis Lang
 */
public class NumDimenFragment extends NumBaseFragment {

    public static final String s_name = "NumDimen";

    public NumDimenFragment() {
    }

    public static NumDimenFragment create() {
        return new NumDimenFragment();
    }

    // ============================================================================================
    // NumBaseFragment methods

    @Override
    public String getName() {
        return s_name;
    }

    @Override
    public void addToList() {
        addNum("app_icon_size", android.R.dimen.app_icon_size);
        addNum("dialog_min_width_major", android.R.dimen.dialog_min_width_major);
        addNum("dialog_min_width_minor", android.R.dimen.dialog_min_width_minor);
        addNum("notification_large_icon_height", android.R.dimen.notification_large_icon_height);
        addNum("notification_large_icon_width", android.R.dimen.notification_large_icon_width);
        addNum("thumbnail_height", android.R.dimen.thumbnail_height);
        addNum("thumbnail_width", android.R.dimen.thumbnail_width);

        /*
        if (false) {
            addNum("toast_y_offset", android.R.dimen.toast_y_offset);
            addNum("status_bar_height", android.R.dimen.status_bar_height);
            addNum("navigation_bar_height", android.R.dimen.navigation_bar_height);
            addNum("navigation_bar_height_landscape",
                    android.R.dimen.navigation_bar_height_landscape);
            addNum("navigation_bar_width", android.R.dimen.navigation_bar_width);
            addNum("navigation_bar_height_car_mode",
                    android.R.dimen.navigation_bar_height_car_mode);
            addNum("navigation_bar_height_landscape_car_mode",
                    android.R.dimen.navigation_bar_height_landscape_car_mode);
            addNum("navigation_bar_width_car_mode", android.R.dimen.navigation_bar_width_car_mode);
            addNum("status_bar_icon_size", android.R.dimen.status_bar_icon_size);
            addNum("status_bar_content_number_size",
                    android.R.dimen.status_bar_content_number_size);
            addNum("status_bar_edge_ignore", android.R.dimen.status_bar_edge_ignore);
            addNum("docked_stack_divider_thickness",
                    android.R.dimen.docked_stack_divider_thickness);
            addNum("docked_stack_divider_insets", android.R.dimen.docked_stack_divider_insets);
            addNum("docked_stack_minimize_thickness",
                    android.R.dimen.docked_stack_minimize_thickness);
            addNum("pip_minimized_visible_size", android.R.dimen.pip_minimized_visible_size);
            addNum("pip_fling_deceleration", android.R.dimen.pip_fling_deceleration);
            addNum("min_xlarge_screen_width", android.R.dimen.min_xlarge_screen_width);
            addNum("password_keyboard_key_height_alpha",
                    android.R.dimen.password_keyboard_key_height_alpha);
            addNum("password_keyboard_key_height_numeric",
                    android.R.dimen.password_keyboard_key_height_numeric);
            addNum("password_keyboard_spacebar_vertical_correction",
                    android.R.dimen.password_keyboard_spacebar_vertical_correction);
            addNum("password_keyboard_horizontalGap",
                    android.R.dimen.password_keyboard_horizontalGap);
            addNum("password_keyboard_verticalGap", android.R.dimen.password_keyboard_verticalGap);
            addNum("keyguard_lockscreen_outerring_diameter",
                    android.R.dimen.keyguard_lockscreen_outerring_diameter);
            addNum("preference_screen_side_margin", android.R.dimen.preference_screen_side_margin);
            addNum("preference_screen_side_margin_negative",
                    android.R.dimen.preference_screen_side_margin_negative);
            addNum("preference_screen_top_margin", android.R.dimen.preference_screen_top_margin);
            addNum("preference_screen_bottom_margin",
                    android.R.dimen.preference_screen_bottom_margin);
            addNum("preference_widget_width", android.R.dimen.preference_widget_width);
            addNum("preference_fragment_padding_bottom",
                    android.R.dimen.preference_fragment_padding_bottom);
            addNum("preference_fragment_padding_side",
                    android.R.dimen.preference_fragment_padding_side);
            addNum("preference_breadcrumb_paddingLeft",
                    android.R.dimen.preference_breadcrumb_paddingLeft);
            addNum("preference_breadcrumb_paddingRight",
                    android.R.dimen.preference_breadcrumb_paddingRight);
            addNum("preference_screen_header_vertical_padding",
                    android.R.dimen.preference_screen_header_vertical_padding);
            addNum("preference_screen_header_padding_side",
                    android.R.dimen.preference_screen_header_padding_side);
            addNum("preference_item_padding_side", android.R.dimen.preference_item_padding_side);
            addNum("preference_item_padding_inner", android.R.dimen.preference_item_padding_inner);
            addNum("preference_child_padding_side", android.R.dimen.preference_child_padding_side);
            addNum("dialog_padding", android.R.dimen.dialog_padding);
            addNum("notification_content_margin_start",
                    android.R.dimen.notification_content_margin_start);
            addNum("notification_content_margin_end",
                    android.R.dimen.notification_content_margin_end);
            addNum("notification_content_picture_margin",
                    android.R.dimen.notification_content_picture_margin);
            addNum("notification_content_plus_picture_margin_end",
                    android.R.dimen.notification_content_plus_picture_margin_end);
            addNum("notification_extra_margin_ambient",
                    android.R.dimen.notification_extra_margin_ambient);
            addNum("notification_action_list_height",
                    android.R.dimen.notification_action_list_height);
            addNum("notification_content_margin_top",
                    android.R.dimen.notification_content_margin_top);
            addNum("notification_content_margin_bottom",
                    android.R.dimen.notification_content_margin_bottom);
            addNum("notification_progress_bar_height",
                    android.R.dimen.notification_progress_bar_height);
            addNum("notification_progress_margin_top",
                    android.R.dimen.notification_progress_margin_top);
            addNum("notification_header_height", android.R.dimen.notification_header_height);
            addNum("notification_header_background_height",
                    android.R.dimen.notification_header_background_height);
            addNum("notification_header_padding_top",
                    android.R.dimen.notification_header_padding_top);
            addNum("notification_header_padding_bottom",
                    android.R.dimen.notification_header_padding_bottom);
            addNum("notification_header_margin_bottom",
                    android.R.dimen.notification_header_margin_bottom);
            addNum("notification_header_icon_margin_end",
                    android.R.dimen.notification_header_icon_margin_end);
            addNum("notification_header_icon_size", android.R.dimen.notification_header_icon_size);
            addNum("notification_header_icon_size_ambient",
                    android.R.dimen.notification_header_icon_size_ambient);
            addNum("notification_header_app_name_margin_start",
                    android.R.dimen.notification_header_app_name_margin_start);
            addNum("notification_header_separating_margin",
                    android.R.dimen.notification_header_separating_margin);
            addNum("notification_header_expand_icon_size",
                    android.R.dimen.notification_header_expand_icon_size);
            addNum("notification_expand_button_padding_top",
                    android.R.dimen.notification_expand_button_padding_top);
            addNum("notification_min_height", android.R.dimen.notification_min_height);

            addNum("notification_header_shrink_min_width",
                    android.R.dimen.notification_header_shrink_min_width);
            addNum("notification_min_content_height",
                    android.R.dimen.notification_min_content_height);
            addNum("media_notification_action_button_size",
                    android.R.dimen.media_notification_action_button_size);
            addNum("media_notification_actions_padding_bottom",
                    android.R.dimen.media_notification_actions_padding_bottom);
            addNum("media_notification_expanded_image_max_size",
                    android.R.dimen.media_notification_expanded_image_max_size);
            addNum("media_notification_expanded_image_margin_bottom",
                    android.R.dimen.media_notification_expanded_image_margin_bottom);
            addNum("media_notification_header_height",
                    android.R.dimen.media_notification_header_height);
            addNum("notification_content_image_margin_end",
                    android.R.dimen.notification_content_image_margin_end);
            addNum("notification_messaging_spacing",
                    android.R.dimen.notification_messaging_spacing);
            addNum("search_view_preferred_width", android.R.dimen.search_view_preferred_width);
            addNum("search_view_preferred_height", android.R.dimen.search_view_preferred_height);
            addNum("alert_dialog_round_padding", android.R.dimen.alert_dialog_round_padding);
            addNum("alert_dialog_title_height", android.R.dimen.alert_dialog_title_height);
            addNum("alert_dialog_button_bar_height",
                    android.R.dimen.alert_dialog_button_bar_height);
            addNum("leanback_alert_dialog_vertical_margin",
                    android.R.dimen.leanback_alert_dialog_vertical_margin);
            addNum("leanback_alert_dialog_horizontal_margin",
                    android.R.dimen.leanback_alert_dialog_horizontal_margin);
            addNum("action_bar_default_height", android.R.dimen.action_bar_default_height);
            addNum("action_bar_icon_vertical_padding",
                    android.R.dimen.action_bar_icon_vertical_padding);
            addNum("action_bar_title_text_size", android.R.dimen.action_bar_title_text_size);
            addNum("action_bar_subtitle_text_size", android.R.dimen.action_bar_subtitle_text_size);
            addNum("action_bar_subtitle_top_margin",
                    android.R.dimen.action_bar_subtitle_top_margin);
            addNum("action_bar_subtitle_bottom_margin",
                    android.R.dimen.action_bar_subtitle_bottom_margin);
            addNum("keyguard_lockscreen_clock_font_size",
                    android.R.dimen.keyguard_lockscreen_clock_font_size);
            addNum("keyguard_lockscreen_status_line_font_size",
                    android.R.dimen.keyguard_lockscreen_status_line_font_size);
            addNum("keyguard_lockscreen_status_line_font_right_margin",
                    android.R.dimen.keyguard_lockscreen_status_line_font_right_margin);
            addNum("keyguard_lockscreen_status_line_clockfont_top_margin",
                    android.R.dimen.keyguard_lockscreen_status_line_clockfont_top_margin);
            addNum("keyguard_lockscreen_status_line_clockfont_bottom_margin",
                    android.R.dimen.keyguard_lockscreen_status_line_clockfont_bottom_margin);
            addNum("keyguard_lockscreen_pin_margin_left",
                    android.R.dimen.keyguard_lockscreen_pin_margin_left);
            addNum("face_unlock_height", android.R.dimen.face_unlock_height);
            addNum("activity_chooser_popup_min_width",
                    android.R.dimen.activity_chooser_popup_min_width);
            addNum("default_gap", android.R.dimen.default_gap);
            addNum("dropdownitem_text_padding_left",
                    android.R.dimen.dropdownitem_text_padding_left);
            addNum("dropdownitem_text_padding_right",
                    android.R.dimen.dropdownitem_text_padding_right);
            addNum("dropdownitem_icon_width", android.R.dimen.dropdownitem_icon_width);
            addNum("textview_error_popup_default_width",
                    android.R.dimen.textview_error_popup_default_width);
            addNum("default_app_widget_padding_left",
                    android.R.dimen.default_app_widget_padding_left);
            addNum("default_app_widget_padding_top",
                    android.R.dimen.default_app_widget_padding_top);
            addNum("default_app_widget_padding_right",
                    android.R.dimen.default_app_widget_padding_right);
            addNum("default_app_widget_padding_bottom",
                    android.R.dimen.default_app_widget_padding_bottom);
            addNum("action_button_min_width", android.R.dimen.action_button_min_width);
            addNum("action_bar_stacked_max_height", android.R.dimen.action_bar_stacked_max_height);
            addNum("action_bar_stacked_tab_max_width",
                    android.R.dimen.action_bar_stacked_tab_max_width);
            addNum("notification_text_size", android.R.dimen.notification_text_size);
            addNum("notification_title_text_size", android.R.dimen.notification_title_text_size);
            addNum("notification_subtext_size", android.R.dimen.notification_subtext_size);
            addNum("notification_top_pad", android.R.dimen.notification_top_pad);
            addNum("notification_top_pad_narrow", android.R.dimen.notification_top_pad_narrow);
            addNum("notification_top_pad_large_text",
                    android.R.dimen.notification_top_pad_large_text);
            addNum("notification_top_pad_large_text_narrow",
                    android.R.dimen.notification_top_pad_large_text_narrow);
            addNum("notification_large_icon_circle_padding",
                    android.R.dimen.notification_large_icon_circle_padding);
            addNum("notification_text_margin_top", android.R.dimen.notification_text_margin_top);
            addNum("notification_inbox_item_top_padding",
                    android.R.dimen.notification_inbox_item_top_padding);
            addNum("notification_badge_size", android.R.dimen.notification_badge_size);
            addNum("kg_security_panel_height", android.R.dimen.kg_security_panel_height);
            addNum("kg_security_view_height", android.R.dimen.kg_security_view_height);
            addNum("kg_widget_view_width", android.R.dimen.kg_widget_view_width);
            addNum("kg_widget_view_height", android.R.dimen.kg_widget_view_height);
            addNum("kg_status_clock_font_size", android.R.dimen.kg_status_clock_font_size);
            addNum("kg_status_date_font_size", android.R.dimen.kg_status_date_font_size);
            addNum("kg_status_line_font_size", android.R.dimen.kg_status_line_font_size);
            addNum("kg_status_line_font_right_margin",
                    android.R.dimen.kg_status_line_font_right_margin);
            addNum("kg_clock_top_margin", android.R.dimen.kg_clock_top_margin);
            addNum("kg_key_horizontal_gap", android.R.dimen.kg_key_horizontal_gap);
            addNum("kg_key_vertical_gap", android.R.dimen.kg_key_vertical_gap);
            addNum("kg_pin_key_height", android.R.dimen.kg_pin_key_height);
            addNum("kg_secure_padding_height", android.R.dimen.kg_secure_padding_height);
            addNum("kg_runway_lights_height", android.R.dimen.kg_runway_lights_height);
            addNum("kg_runway_lights_vertical_padding",
                    android.R.dimen.kg_runway_lights_vertical_padding);
            addNum("kg_widget_pager_horizontal_padding",
                    android.R.dimen.kg_widget_pager_horizontal_padding);
            addNum("kg_widget_pager_top_padding", android.R.dimen.kg_widget_pager_top_padding);
            addNum("kg_widget_pager_bottom_padding",
                    android.R.dimen.kg_widget_pager_bottom_padding);
            addNum("kg_runway_lights_top_margin", android.R.dimen.kg_runway_lights_top_margin);
            addNum("accessibility_touch_slop", android.R.dimen.accessibility_touch_slop);
            addNum("accessibility_magnification_indicator_width",
                    android.R.dimen.accessibility_magnification_indicator_width);
            addNum("keyguard_muliuser_selector_margin",
                    android.R.dimen.keyguard_muliuser_selector_margin);
            addNum("keyguard_avatar_frame_stroke_width",
                    android.R.dimen.keyguard_avatar_frame_stroke_width);
            addNum("keyguard_avatar_frame_shadow_radius",
                    android.R.dimen.keyguard_avatar_frame_shadow_radius);
            addNum("keyguard_avatar_size", android.R.dimen.keyguard_avatar_size);
            addNum("keyguard_avatar_name_size", android.R.dimen.keyguard_avatar_name_size);
            addNum("kg_edge_swipe_region_size", android.R.dimen.kg_edge_swipe_region_size);
            addNum("kg_squashed_layout_threshold", android.R.dimen.kg_squashed_layout_threshold);
            addNum("kg_small_widget_height", android.R.dimen.kg_small_widget_height);
            addNum("subtitle_corner_radius", android.R.dimen.subtitle_corner_radius);
            addNum("subtitle_shadow_radius", android.R.dimen.subtitle_shadow_radius);
            addNum("subtitle_shadow_offset", android.R.dimen.subtitle_shadow_offset);
            addNum("subtitle_outline_width", android.R.dimen.subtitle_outline_width);
            addNum("immersive_mode_cling_width", android.R.dimen.immersive_mode_cling_width);
            addNum("resolver_max_width", android.R.dimen.resolver_max_width);
            addNum("circular_display_mask_thickness",
                    android.R.dimen.circular_display_mask_thickness);
            addNum("lock_pattern_dot_line_width", android.R.dimen.lock_pattern_dot_line_width);
            addNum("lock_pattern_dot_size", android.R.dimen.lock_pattern_dot_size);
            addNum("lock_pattern_dot_size_activated",
                    android.R.dimen.lock_pattern_dot_size_activated);
            addNum("text_handle_min_size", android.R.dimen.text_handle_min_size);
        }
        */
    }
}


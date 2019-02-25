/*
 * jquery.watermark.js
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN
 * NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE
 * USE OR OTHER DEALINGS IN THE SOFTWARE.
 * 
 */
(function ($) {

    $.extend($, {
        clearwatermarks: function () {
            $("[wmwrap='true']").find("input,textarea").watermark({remove: true});
        },
        addwatermarks: function () {
            $("[watermark]").each(function (num, el) {
                $(el).watermark($(el).attr("watermark"));
            });
        },
        watermark: function (o) {
            var defaultOffsetLeft = 3;
            // o.el is input: fr-texteditor
            o.el = $(o.el);
            if (o.remove) {
                if ($.browser.msie) {
                    if (o.el.parent().attr("wmwrap") == 'true') {
                        o.el.parent().replaceWith(o.el);
                    }
                } else {
                    o.el.removeAttr('placeholder');
                }
            } else if (o.clear) {
                if ($.browser.msie) {
                    if ($('label.fr-watermark-label', o.el.parent())) {
                        $('label.fr-watermark-label', o.el.parent()).hide();
                    }
                } else {
                    o.el.attr('placeholder', "");
                }
            } else {
                if ($.browser.msie) {
                    if (o.el.parent().attr("wmwrap") != 'true') {
                        o.el = o.el.wrap("<span wmwrap='true'/>");
                        var l = $("<label/>").addClass('fr-watermark-label');
                        if (o.html) {
                            l.html(o.html);
                        }
                        if (o.cls) {
                            l.addClass(o.cls);
                        }
                        if (o.css) {
                            l.css(o.css);
                        }
                        l.css({
                            position: "absolute",
                            left: defaultOffsetLeft + 'px',
                            top: "",
                            display: "inline",
                            cursor: "text",
                            width: o.el.width(),
                            height: o.el.height(),
                            overflow: "hidden",
                            "font-size": "9pt",
                            "white-space": "nowrap"
                        });
                        //直接显示控件的时候position需要变成relative
                        if (o.isEditable) {
                            l.css('left', o.offsetLeft || defaultOffsetLeft);
                        }
                        l.css("line-height", (o.el.height() + ($.support.boxModel ? 4 : 0)) + "px");

                        if (!o.cls && !o.css) {
                            l.css("color", l.getwatermarkcolor());
                        }

                        var focus = function () {
                            l.hide();
                        };

                        var blur = function () {
                            if (!o.el.val()) {
                                l.show();
                            } else {
                                l.hide();
                            }
                        };

                        var click = function () {
                            o.el.focus();
                        };

                        if (o.inherit) {
                            if (typeof o.inherit == "string") {
                                l.css(o.inherit, o.el.css(o.inherit));
                            } else {
                                for (var x = 0; x < o.inherit.length; x++) {
                                    l.css(o.inherit[x], o.el.css(o.inherit[x]));
                                }
                            }
                        }
                        if (!o.el.attr("disabled")) {
                            o.el.focus(focus).blur(blur);
                            // 绑定值改变事件
                            o.el[0].onpropertychange = function () {
                                if (!o.el.val()) {
                                    l.show();
                                } else {
                                    l.hide();
                                }
                            }
                            l.click(click);
                        }
                        o.el.before(l);
                        if (o.el.val()) {
                            l.hide();
                        }
                    }
                } else {
                    var clicked = false;
                    o.el.attr('placeholder', o.html);
                    o.el.focus(function () {
                        o.el.removeAttr('placeholder');
                        // 如果是firefox
                        if (FR.Browser.r.gecko) {
                            if (!clicked) {
                                clicked = true;
                                $(this).click();
                            } else {
                                clicked = false;
                            }
                        }
                    }).blur(function () {
                        o.el.attr('placeholder', o.html);
                    })
                }
            }
            return o.el;
        }
    });

    $.fn.watermark = function (o, isEditable) {
        var offestLeft = this.offset().left;
        return this.each(function () {
            if (typeof(o) == "string") {
                try {
                    o = eval("(" + o + ")");
                } catch (ex) {
                    o = {html: o};
                }
                if (typeof(o) == "number") {
                    o = {html: o};
                }
            }
            o.el = this;
            o.offsetLeft = offestLeft;
            o.isEditable = isEditable;
            return $.watermark(o);
        });
    };
    $.fn.watermarkValueChange = function () {

    };
    // 用"#ccc"赋值给color的话再取出来就是rgb(204, 204, 204)了
    $.fn.getwatermarkcolor = function () {
        return "rgb(204, 204, 204)";
    };
})(jQuery);

$().ready(function () {
    $.addwatermarks();
});
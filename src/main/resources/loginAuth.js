AJS.$(function () {

    if (AJS.$("#login-form").length) {
        loadCorpLogin(AJS.$("#login-form"));
    } else {
        AJS.$("iframe").ready(function () {

            var iframe = AJS.$("#gadget-0")
            iframe.load(function () {
                loginForm = AJS.$("#" + iframe[0].id).contents().find("#loginform")
                loadCorpLogin(loginForm);
            });
        });
    }

    function loadCorpLogin(loginForm) {
        if (loginForm.length == 1) {
            loginFormId = loginForm[0].id
            loginForm.hide();

            AJS.$.ajax({
                url: AJS.contextPath() + "/rest/saml2-plugin/1.0/idps",
                type: "GET",
                success: function(idps) {
                    if (!idps.length) return;
                    var fs = null;
                    if (loginFormId == "login-form") {
                        fs = AJS.$('<fieldset class="group"><legend>Вход через Single-Sign-On</legend></fieldset>').insertBefore(AJS.$("#" + loginFormId + " .form-body"));
                    } else {
                        fs = AJS.$('<fieldset class="group"><legend>Вход через Single-Sign-On</legend></fieldset>').insertBefore(AJS.$("#gadget-0"));
                    }
                    if (fs != null) {
                        var btns = AJS.$('<p class="aui-buttons"></p>');
                        idps.forEach(function (idp) {
                            btns.append(AJS.$('<a class="aui-button" href="' + AJS.contextPath() + '/plugins/servlet/saml/auth?entityId=' + idp + '" style="align:center;">' + idp + '</a>'));
                        });
                        fs.append(btns);
                    }
                }
            });

            var query = location.search.substr(1);
            query.split("&").forEach(function (part) {
                var item = part.split("=");
                if (item.length == 2 && item[0] == "samlerror") {
                    var errorKeys = {};
                    errorKeys["general"] = "Общая ошибка SAML конфигурации";
                    errorKeys["user_not_found"] = "Пользователь не найден";
                    errorKeys["plugin_exception"] = "Внутренняя ошибка расширения SAML";
                    errorKeys["auth_error"] = "Ошибка авторизации";
                    errorKeys["unknown_idp"] = "Identity Provider не настроен";
                    loginForm.show();
                    var message = '<div class="aui-message closeable error">' + errorKeys[item[1]] + '</div>';
                    AJS.$(message).insertBefore(loginForm);
                }
            });

            if (location.search == '?logout=true') {
                $.ajax({
                    url: AJS.contextPath() + "/plugins/servlet/saml/getajaxconfig?param=logoutUrl",
                    type: "GET",
                    error: function () {
                    },
                    success: function (response) {
                        if (response != "") {
                            AJS.$('<p>Please wait while we redirect you to your company log out page</p>').insertBefore(loginForm);
                            window.location.href = response;
                            return;
                        }
                    }
                });
                return;
            }

            AJS.$.ajax({
                url: AJS.contextPath() + "/plugins/servlet/saml/getajaxconfig?param=idpRequired",
                type: "GET",
                error: function () {
                },
                success: function (response) {
                    if (response == "true") {
                        // AJS.$('<img src="download/resources/com.bitium.confluence.SAML2Plugin/images/progress.png"/>').insertBefore(AJS.$(".aui.login-form-container"));
                        AJS.$('<p><!--Please wait while we redirect you to your company log in page-->Пожалуйста, подождите пока происходит перенаправление на страницу входа вашей компании</p>').insertBefore(loginForm);
                        window.location.href = AJS.contextPath() + '/plugins/servlet/saml/auth';

                    } else {
                        loginForm.show();
                    }
                }
            });

        }
    }

});

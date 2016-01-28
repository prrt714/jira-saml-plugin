AJS.toInit(function () {
//    client = new ZeroClipboard(document.getElementById("copy-button"));
//
//    client.on("copy", function (event) {
//        var clipboard = event.clipboardData;
//        var samlField = document.getElementById("samlEndpoint");
//        clipboard.setData("text/plain", samlField.value);
//        var endpointCopied = document.getElementById("endpoint-copied");
//        endpointCopied.style.visibility = "visible";
//        setTimeout(function () {
//            endpointCopied.style.visibility = "hidden";
//        }, 2000);
//        return false;
//    });
    var toggleRemoveFromGroups = function (e) {
        document.getElementById("removeFromGroups").disabled = !e.target.checked;
    };
    document.getElementById("autoCreateUser").addEventListener("change", toggleRemoveFromGroups);

    AJS.$("#idps").auiSelect2().on("change", function(e) {
        window.location.search = 'entityId=' + e.val;
    });

    AJS.$("#deleteIdP").on("click", function(e) {
        AJS.$.ajax({
            url: AJS.contextPath() + "/rest/saml2-plugin/1.0/?entityId=" + AJS.$("#idps").val(),
            type: "DELETE",
            success: function(result) {
                window.location.reload();
            }
        });
    });

});
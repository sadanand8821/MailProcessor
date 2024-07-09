// Initialize redirects array if not already set
if (!pm.collectionVariables.get("redirects")) {
    pm.collectionVariables.set("redirects", JSON.stringify([]));
}

// Get the current redirects array
let redirects = pm.collectionVariables.get("redirects") ? JSON.parse(pm.collectionVariables.get("redirects")) : [];
console.log("Initial redirects:", redirects);

// Set the request URL to the latest redirect if there is one
if (redirects.length > 0) {
    let lastRedirectUrl = redirects[redirects.length - 1];
    pm.request.url = lastRedirectUrl;
    console.log("Setting request URL to:", lastRedirectUrl);
} else {
    console.log("Using initial request URL");
}

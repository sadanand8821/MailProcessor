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



// Log to confirm the test script is running
console.log("Test script running");

// Get redirects array from collection variables
let redirects = pm.collectionVariables.get("redirects") ? JSON.parse(pm.collectionVariables.get("redirects")) : [];
console.log("Current redirects:", redirects);

// Log all response headers
pm.response.headers.all().forEach(function(header) {
    console.log(header.key + ": " + header.value);
});

// Check if the response has a 'Location' header
if (pm.response.headers.has('Location')) {
    // Push the Location header value to the redirects array
    let locationHeader = pm.response.headers.get('Location');
    console.log("Found Location header:", locationHeader);
    redirects.push(locationHeader);
    
    // Update the collection variable with the new redirects array
    pm.collectionVariables.set("redirects", JSON.stringify(redirects));
    console.log("Updated redirects:", JSON.stringify(redirects));
    
    // Set the next request to run, which will use the new URL
    postman.setNextRequest(pm.info.requestName);
} else {
    console.log("No Location header found in the response or final response received");
    
    // Clear redirects if this is the final response
    pm.collectionVariables.unset("redirects");
}

// Log the response code
console.log("Response code:", pm.response.code);

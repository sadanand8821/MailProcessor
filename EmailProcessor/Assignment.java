const postmanRequest = require('postman-request');

function followRedirects(url, callback) {
    postmanRequest({
        url: url,
        method: 'GET',
        followRedirect: false,
        followAllRedirects: true,
        maxRedirects: 10 // Adjust as needed
    }, (error, response, body) => {
        if (error) {
            console.error('Request failed:', error);
        } else if (response.statusCode === 401) {
            console.log('Final URL before 401:', response.request.uri.href);
        } else if (response.statusCode >= 300 && response.statusCode < 400) {
            const redirectUrl = response.headers.location;
            if (redirectUrl) {
                followRedirects(redirectUrl, callback);
            } else {
                console.log('Redirect location not found');
            }
        } else {
            console.log('Final URL:', response.request.uri.href);
        }
    });
}

const initialUrl = 'https://your-initial-request-url.com';
followRedirects(initialUrl);

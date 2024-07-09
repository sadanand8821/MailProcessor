const axios = require('axios');

async function followRedirects(url) {
    try {
        const response = await axios.get(url, {
            maxRedirects: 10, // Adjust this as needed
            validateStatus: function (status) {
                return status < 400; // Resolve only if the status code is less than 400
            }
        });
        console.log('Final URL:', response.request.res.responseUrl);
    } catch (error) {
        if (error.response) {
            console.log('Final URL before 401:', error.response.request.res.responseUrl);
        } else {
            console.error('Error:', error.message);
        }
    }
}

followRedirects('https://your-initial-request-url.com');


const request = require('postman-request');

function followRedirects(url, callback) {
    request({
        url: url,
        method: 'GET',
        followRedirect: false,
        followAllRedirects: true,
        maxRedirects: 10 // Adjust as needed
    }, (error, response, body) => {
        if (error) {
            console.error('Request failed:', error);
        } else if (response.statusCode === 401) {
            console.log('Final URL before 401:', response.request.href);
        } else if (response.statusCode >= 300 && response.statusCode < 400) {
            const redirectUrl = response.headers.location;
            if (redirectUrl) {
                followRedirects(redirectUrl, callback);
            } else {
                console.log('Redirect location not found');
            }
        } else {
            console.log('Final URL:', response.request.href);
        }
    });
}

followRedirects('https://your-initial-request-url.com');

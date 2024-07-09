pm.sendRequest({
    url: 'https://your-initial-request-url.com',
    method: 'GET',
    followRedirects: true
}, function (err, res) {
    if (err) {
        console.log(err);
    } else {
        console.log('Final URL:', res.responseHeaders.Location || res.responseHeaders['location']);
    }
});

document.querySelectorAll('.yt-video').forEach(function (container) {
    var btn = container.querySelector('.yt-facade');
    if (!btn) return;

    btn.addEventListener('click', function () {
        var id    = container.dataset.videoId;
        var start = container.dataset.start || '0';
        var title = container.dataset.title || 'Video';

        var iframe = document.createElement('iframe');
        iframe.src = 'https://www.youtube-nocookie.com/embed/' + encodeURIComponent(id) +
            '?autoplay=1&start=' + encodeURIComponent(start) + '&rel=0';
        iframe.title = title;
        iframe.setAttribute('allow',
            'accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share');
        iframe.setAttribute('allowfullscreen', '');
        iframe.setAttribute('frameborder', '0');
        iframe.style.width = '100%';
        iframe.style.height = '100%';
        iframe.style.border = '0';

        container.replaceChildren(iframe);
    });
});
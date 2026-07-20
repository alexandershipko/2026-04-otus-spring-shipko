function escapeHtml(value) {
    const div = document.createElement('div');
    div.textContent = value;
    return div.innerHTML;
}

async function fetchJson(url, options) {
    const response = await fetch(url, options);

    if (!response.ok) {
        let error;

        try {
            error = await response.json();
        } catch (ignored) {
            error = {};
        }

        const requestError = new Error(error.message || `Request to ${url} failed with status ${response.status}`);
        requestError.errors = error.errors;
        throw requestError;
    }

    if (response.status === 204) {
        return null;
    }

    return response.json();
}

function showLoadError(error) {
    const message = error && error.message ? error.message : window.i18n.loadError;
    document.getElementById('error-message').textContent = message;
}

document.addEventListener('DOMContentLoaded', () => {
    fetchJson('/api/genres').then(renderGenres).catch(showLoadError);
});

function renderGenres(genres) {
    const tbody = document.getElementById('genres-body');
    tbody.innerHTML = '';

    genres.forEach(genre => {
        const row = document.createElement('tr');
        row.innerHTML = `<td>${genre.id}</td><td>${escapeHtml(genre.name)}</td>`;
        tbody.appendChild(row);
    });
}

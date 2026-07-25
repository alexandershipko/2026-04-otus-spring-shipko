document.addEventListener('DOMContentLoaded', () => {
    fetchJson('/api/authors').then(renderAuthors).catch(showLoadError);
});

function renderAuthors(authors) {
    const tbody = document.getElementById('authors-body');
    tbody.innerHTML = '';

    authors.forEach(author => {
        const row = document.createElement('tr');
        row.innerHTML = `<td>${author.id}</td><td>${escapeHtml(author.fullName)}</td>`;
        tbody.appendChild(row);
    });
}

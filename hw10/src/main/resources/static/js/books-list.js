document.addEventListener('DOMContentLoaded', loadBooks);

function loadBooks() {
    fetchJson('/api/books').then(renderBooks).catch(showLoadError);
}

function renderBooks(books) {
    const tbody = document.getElementById('books-body');
    tbody.innerHTML = '';

    books.forEach(book => tbody.appendChild(buildBookRow(book)));
}

function buildBookRow(book) {
    const genreNames = book.genres.map(genre => genre.name).join(', ');

    const row = document.createElement('tr');
    row.innerHTML = `
        <td>${book.id}</td>
        <td><a href="/books/${book.id}">${escapeHtml(book.title)}</a></td>
        <td>${escapeHtml(book.author.fullName)}</td>
        <td>${escapeHtml(genreNames)}</td>
        <td>
            <a class="btn" href="/books/${book.id}/edit">${window.i18n.edit}</a>
            <button type="button" class="btn">${window.i18n.delete}</button>
        </td>
    `;

    row.querySelector('button').addEventListener('click', () => deleteBook(book.id));

    return row;
}

function deleteBook(id) {
    if (!confirm(window.i18n.deleteConfirm)) {
        return;
    }

    fetchJson(`/api/books/${id}`, {method: 'DELETE'})
        .then(loadBooks)
        .catch(showLoadError);
}

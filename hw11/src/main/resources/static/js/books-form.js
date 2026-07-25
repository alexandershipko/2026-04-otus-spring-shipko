const editMatch = window.location.pathname.match(/^\/books\/(\d+)\/edit$/);
const bookId = editMatch ? editMatch[1] : null;
const isEdit = bookId !== null;

document.addEventListener('DOMContentLoaded', () => {
    document.title = isEdit ? window.i18n.titleEdit : window.i18n.titleNew;
    document.getElementById('page-heading').textContent = document.title;
    document.getElementById('book-form').addEventListener('submit', submitForm);

    loadFormData();
});

function loadFormData() {
    Promise.all([fetchJson('/api/authors'), fetchJson('/api/genres')])
        .then(([authors, genres]) => {
            renderAuthors(authors);
            renderGenres(genres);

            if (isEdit) {
                loadBook();
            }
        })
        .catch(showLoadError);
}

function renderAuthors(authors) {
    const select = document.getElementById('author-id');

    authors.forEach(author => {
        const option = document.createElement('option');
        option.value = author.id;
        option.textContent = author.fullName;
        select.appendChild(option);
    });
}

function renderGenres(genres) {
    const container = document.getElementById('genres-container');

    genres.forEach(genre => {
        const label = document.createElement('label');

        const checkbox = document.createElement('input');
        checkbox.type = 'checkbox';
        checkbox.value = genre.id;

        label.appendChild(checkbox);
        label.appendChild(document.createTextNode(' ' + genre.name));

        container.appendChild(label);
        container.appendChild(document.createElement('br'));
    });
}

function loadBook() {
    fetchJson(`/api/books/${bookId}`)
        .then(book => {
            document.getElementById('title').value = book.title;
            document.getElementById('author-id').value = book.author.id;

            const genreIds = new Set(book.genres.map(genre => String(genre.id)));
            document.querySelectorAll('#genres-container input[type=checkbox]').forEach(checkbox => {
                checkbox.checked = genreIds.has(checkbox.value);
            });
        })
        .catch(showLoadError);
}

function submitForm(event) {
    event.preventDefault();
    clearErrors();

    const payload = {
        title: document.getElementById('title').value,
        authorId: Number(document.getElementById('author-id').value),
        genreIds: Array.from(document.querySelectorAll('#genres-container input[type=checkbox]:checked'))
            .map(checkbox => Number(checkbox.value))
    };

    const url = isEdit ? `/api/books/${bookId}` : '/api/books';
    const method = isEdit ? 'PUT' : 'POST';

    fetchJson(url, {
        method,
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify(payload)
    }).then(() => {
        window.location.href = '/books';
    }).catch(showErrors);
}

function clearErrors() {
    document.getElementById('title-error').textContent = '';
    document.getElementById('author-error').textContent = '';
    document.getElementById('genres-error').textContent = '';
}

function showErrors(error) {
    if (!error.errors) {
        showLoadError(error);
        return;
    }

    if (error.errors.title) {
        document.getElementById('title-error').textContent = error.errors.title;
    }
    if (error.errors.authorId) {
        document.getElementById('author-error').textContent = error.errors.authorId;
    }
    if (error.errors.genreIds) {
        document.getElementById('genres-error').textContent = error.errors.genreIds;
    }
}

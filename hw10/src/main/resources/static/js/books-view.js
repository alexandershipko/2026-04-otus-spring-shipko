const bookId = window.location.pathname.match(/^\/books\/(\d+)$/)[1];

document.addEventListener('DOMContentLoaded', () => {
    document.getElementById('edit-link').href = `/books/${bookId}/edit`;
    document.getElementById('delete-btn').addEventListener('click', deleteBook);
    document.getElementById('comment-form').addEventListener('submit', addComment);

    loadBook();
    loadComments();
});

function loadBook() {
    fetchJson(`/api/books/${bookId}`)
        .then(book => {
            document.title = book.title;
            document.getElementById('book-title').textContent = book.title;
            document.getElementById('book-author').textContent = book.author.fullName;
            document.getElementById('book-genres').textContent = book.genres.map(genre => genre.name).join(', ');
        })
        .catch(showLoadError);
}

function loadComments() {
    fetchJson(`/api/books/${bookId}/comments`).then(renderComments).catch(showLoadError);
}

function renderComments(comments) {
    const list = document.getElementById('comments-list');
    list.innerHTML = '';

    if (comments.length === 0) {
        const li = document.createElement('li');
        li.textContent = window.i18n.noComments;
        list.appendChild(li);
        return;
    }

    comments.forEach(comment => list.appendChild(buildCommentItem(comment)));
}

function buildCommentItem(comment) {
    const li = document.createElement('li');
    li.textContent = comment.text + ' ';

    const deleteBtn = document.createElement('button');
    deleteBtn.type = 'button';
    deleteBtn.className = 'btn';
    deleteBtn.textContent = window.i18n.deleteComment;
    deleteBtn.addEventListener('click', () => deleteComment(comment.id));

    li.appendChild(deleteBtn);

    return li;
}

function addComment(event) {
    event.preventDefault();

    const input = document.getElementById('comment-text');
    const text = input.value;

    fetchJson(`/api/books/${bookId}/comments`, {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({text})
    }).then(() => {
        input.value = '';
        loadComments();
    }).catch(showLoadError);
}

function deleteComment(commentId) {
    if (!confirm(window.i18n.deleteCommentConfirm)) {
        return;
    }

    fetchJson(`/api/books/${bookId}/comments/${commentId}`, {method: 'DELETE'})
        .then(loadComments)
        .catch(showLoadError);
}

function deleteBook() {
    if (!confirm(window.i18n.deleteConfirm)) {
        return;
    }

    fetchJson(`/api/books/${bookId}`, {method: 'DELETE'})
        .then(() => {
            window.location.href = '/books';
        })
        .catch(showLoadError);
}

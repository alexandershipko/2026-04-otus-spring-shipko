package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.dto.BookCommentCreateDto;
import ru.otus.hw.dto.BookCommentDto;
import ru.otus.hw.dto.BookCommentUpdateDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.BookComment;
import ru.otus.hw.repositories.BookCommentRepository;
import ru.otus.hw.repositories.BookRepository;

import java.util.List;

@RequiredArgsConstructor
@Service
public class BookCommentServiceImpl implements BookCommentService {

    private final BookCommentRepository bookCommentRepository;

    private final BookRepository bookRepository;

    private final AclPermissionService aclPermissionService;

    @Override
    @Transactional(readOnly = true)
    public BookCommentDto findById(long id) {
        var comment = bookCommentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Comment with id %d not found".formatted(id)));

        return toBookCommentDto(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookCommentDto> findAllByBookId(long bookId) {
        return bookCommentRepository.findAllByBookId(bookId).stream()
                .map(BookCommentServiceImpl::toBookCommentDto)
                .toList();
    }

    @Override
    @Transactional
    public BookCommentDto insert(BookCommentCreateDto bookCommentCreateDto) {
        var book = bookRepository.findById(bookCommentCreateDto.getBookId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Book with id %d not found".formatted(bookCommentCreateDto.getBookId())));
        var comment = new BookComment(0, bookCommentCreateDto.getText(), book);
        var savedComment = bookCommentRepository.save(comment);

        aclPermissionService.grantOwnerPermissions(savedComment, BasePermission.WRITE, BasePermission.DELETE);

        return toBookCommentDto(savedComment);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN') or "
            + "hasPermission(#bookCommentUpdateDto.id, 'ru.otus.hw.models.BookComment', 'WRITE')")
    public BookCommentDto update(BookCommentUpdateDto bookCommentUpdateDto) {
        var comment = bookCommentRepository.findById(bookCommentUpdateDto.getId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Comment with id %d not found".formatted(bookCommentUpdateDto.getId())));
        comment.setText(bookCommentUpdateDto.getText());

        return toBookCommentDto(bookCommentRepository.save(comment));
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN') or hasPermission(#id, 'ru.otus.hw.models.BookComment', 'DELETE')")
    public void deleteById(long id) {
        bookCommentRepository.deleteById(id);

        aclPermissionService.deleteAcl(BookComment.class, id);
    }

    private static BookCommentDto toBookCommentDto(BookComment comment) {
        return new BookCommentDto(comment.getId(), comment.getText());
    }

}

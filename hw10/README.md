Требования к реализации:

0. Доделать обработку ошибок! @ExceptionHandler / @ControllerAdvice - должен быть JSON c ошибкой

@ControllerAdvice
public class RestResponseEntityExceptionHandler
extends ResponseEntityExceptionHandler {
@ExceptionHandler(value
= { IllegalArgumentException.class, IllegalStateException.class })
protected ResponseEntity<Object> handleConflict(
RuntimeException ex, WebRequest request) {
String bodyOfResponse = "This should be application specific";
return handleExceptionInternal(ex, bodyOfResponse,
new HttpHeaders(), HttpStatus.CONFLICT, request);
}
}

1. Для получения данных на страницах приложения использовать JavaScript, fetch api и REST-контроллеры;
2. Минимум: переделать CRUD операции над книгами;
3. URL эндпойнтов должны соответствовать ресурсному стилю REST (см. пример типового api на соответствующем слайде презентации к лекции);
4. Действия над сущностями должны быть выражены исключительно через HTTP-методы. Глаголов в URL быть не должно;
5. Протестировать все эндпойнты REST-контроллеров с помощью @WebMvcTest и моков сервисов;
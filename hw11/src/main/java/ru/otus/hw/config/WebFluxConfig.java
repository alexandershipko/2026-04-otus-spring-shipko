package ru.otus.hw.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.i18n.LocaleContext;
import org.springframework.context.i18n.SimpleLocaleContext;
import org.springframework.http.ResponseCookie;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.i18n.LocaleContextResolver;

import java.net.URI;
import java.time.Duration;
import java.util.Locale;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class WebFluxConfig {

    private static final String LOCALE_COOKIE_NAME = "locale";

    private static final String LOCALE_ATTRIBUTE_NAME = WebFluxConfig.class.getName() + ".LOCALE";

    private static final String LOCALE_PARAM_NAME = "lang";

    private static final Locale DEFAULT_LOCALE = Locale.forLanguageTag("ru");

    private static final int LOCALE_COOKIE_MAX_AGE_DAYS = 365;

    @Bean
    public LocaleContextResolver localeContextResolver() {
        return new CookieLocaleContextResolver();
    }

    private static final class CookieLocaleContextResolver implements LocaleContextResolver {

        @Override
        public LocaleContext resolveLocaleContext(ServerWebExchange exchange) {
            var requestedLocale = (Locale) exchange.getAttributes().get(LOCALE_ATTRIBUTE_NAME);

            if (requestedLocale != null) {
                return new SimpleLocaleContext(requestedLocale);
            }

            var cookie = exchange.getRequest().getCookies().getFirst(LOCALE_COOKIE_NAME);
            var locale = cookie != null ? StringUtils.parseLocale(cookie.getValue()) : null;

            return new SimpleLocaleContext(locale != null ? locale : DEFAULT_LOCALE);
        }

        @Override
        public void setLocaleContext(ServerWebExchange exchange, LocaleContext localeContext) {
            var locale = localeContext != null ? localeContext.getLocale() : null;

            if (locale == null) {
                return;
            }

            exchange.getAttributes().put(LOCALE_ATTRIBUTE_NAME, locale);
            exchange.getResponse().addCookie(ResponseCookie.from(LOCALE_COOKIE_NAME, locale.toString())
                    .maxAge(Duration.ofDays(LOCALE_COOKIE_MAX_AGE_DAYS))
                    .path("/")
                    .build());
        }

    }

    @Bean
    public WebFilter localeChangeFilter(LocaleContextResolver localeContextResolver) {
        return (exchange, chain) -> {
            var langParam = exchange.getRequest().getQueryParams().getFirst(LOCALE_PARAM_NAME);

            if (StringUtils.hasText(langParam)) {
                var locale = StringUtils.parseLocale(langParam);
                localeContextResolver.setLocaleContext(exchange, new SimpleLocaleContext(locale));
            }

            return chain.filter(exchange);
        };
    }

    @Bean
    public RouterFunction<ServerResponse> pageRoutes() {
        return route(GET("/"), request -> ServerResponse.permanentRedirect(URI.create("/books")).build())
                .andRoute(GET("/books"), request -> ServerResponse.ok().render("books/list"))
                .andRoute(GET("/books/new"), request -> ServerResponse.ok().render("books/form"))
                .andRoute(GET("/books/{id}"), request -> ServerResponse.ok().render("books/view"))
                .andRoute(GET("/books/{id}/edit"), request -> ServerResponse.ok().render("books/form"))
                .andRoute(GET("/authors"), request -> ServerResponse.ok().render("authors/list"))
                .andRoute(GET("/genres"), request -> ServerResponse.ok().render("genres/list"));
    }

}

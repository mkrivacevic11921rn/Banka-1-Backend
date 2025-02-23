package com.banka1.user.aspect;

import com.banka1.user.model.helper.Permission;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotacija koja se vezuje za kontroler metode za koje je potrebna autorizacija.
 * Ako ijedna od potrebnih permisija nije zadovoljena, korisnik neće biti autorizovan.
 * Date metode moraju primati odvojen parametar koji sadrži autorizacioni header i <b>ima isto ime</b>.
 * <p></p>
 * Primer korišćenja:
 * <p></p>
 * <pre>
 * {@code
 * @Authorization(permissions = { Permission.READ_EMPLOYEE })
 * @GetMapping(...)
 * public ResponseEntity<?> requireReadUserPerms(@RequestHeader("Authorization") String authorization, ...) {
 *     ...
 * }
 * }</pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Authorization {
    Permission[] permissions() default { };
}
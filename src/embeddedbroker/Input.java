package embeddedbroker;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Input {

	boolean required() default true;

	TransferCommand value() default TransferCommand.STORE;

	enum TransferCommand {PUT, STORE}

}

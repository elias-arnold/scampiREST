package querrilla.utils;

/**
 * Optional can contain a value or not.
 *
 * @author teemuk
 */
public class Optional<T> {
    private final T   value;

    public Optional( T value ) {
        this.value = value;
    }

    public boolean isPresent() {
        return ( this.value != null );
    }

    public T get() {
        if ( !this.isPresent() ) {
            throw new IllegalStateException( "Value is not present." );
        }
        return this.value;
    }
}

package tomb.test

/**
 * This closure is used as a marker used with the
 * Spock framework's @Requires annotation
 */
class S3Configured extends Closure<Boolean> {

    S3Configured(Object owner, Object thisObject) {
        super(owner, thisObject)
    }

    Boolean doCall() {
        return System.getenv('TOMB_KEY') &&
               System.getenv('TOMB_SECRET') &&
               System.getenv('TOMB_BUCKET')
    }

}

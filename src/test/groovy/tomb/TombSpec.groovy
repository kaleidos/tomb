package tomb

import spock.lang.*
import java.nio.file.Paths

import tomb.test.S3Configured
import tomb.exception.FilesystemException
import com.amazonaws.services.s3.AmazonS3Client
import tomb.utils.AmazonS3Utils

class TombSpec extends Specification {

    void 'Obtain a LocalFilesystem'() {
        given: 'A base path'
            def basePath = Paths.get('/')

        when: 'getting the LocalFilesystem'
            def fs = Tomb.getLocalFilesystem(basePath)

        then: 'everything should be ok'
            notThrown RuntimeException

        and: 'the fs path should be correct'
            fs.basePath == basePath
    }

    void 'Obtain a LocalFilesystem with a nonexistent path'() {
        given: 'A base path'
            def basePath = Paths.get('/asdf')

        and: "being sure that it doesn't exists"
            assert !basePath.toFile().exists()

        when: 'getting the LocalFilesystem'
            def fs = Tomb.getLocalFilesystem(basePath)

        then: 'an exception should be thrown'
            thrown FilesystemException
    }

    void 'Obtain a LocalFilesystem with a path pointing to a file'() {
        given: 'A base path'
            def basePath = File.createTempFile('tomb_', '_tmp').toPath()

        and: "being sure that it exists"
            assert basePath.toFile().exists()

        when: 'getting the LocalFilesystem'
            def fs = Tomb.getLocalFilesystem(basePath)

        then: 'an exception should be thrown'
            thrown FilesystemException
    }

    @Requires(S3Configured)
    void 'Obtain an AmazonS3Filesystem'() {
        given: 'Some basic data'
            def basePath = Paths.get('')
            def key = System.getenv('TOMB_KEY')
            def secret = System.getenv('TOMB_SECRET')
            def bucket = System.getenv('TOMB_BUCKET')

        when: 'trying to obtain the filesystem'
            def result = Tomb.getAmazonS3Filesystem(key, secret, bucket, basePath)

        then: 'everything should be ok'
            result.basePath == basePath
            result.bucket == bucket
            result.s3Client
    }

}

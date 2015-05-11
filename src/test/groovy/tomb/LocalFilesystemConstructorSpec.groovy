package tomb

import spock.lang.*
import java.nio.file.Paths

class LocalFilesystemConstructorSpec extends Specification {

    void 'Instantiating the LocalFilesystem class'() {
        given: 'A base path'
            def basePath = Paths.get('/')

        when: 'instantiating the LocalFilesystem class'
            def fs = new LocalFilesystem(basePath)

        then: 'everything should be ok'
            notThrown RuntimeException

        and: 'the fs path should be correct'
            fs.basePath == basePath
    }

    void 'Instantiating the LocalFilesystem class with a nonexistent path'() {
        given: 'A base path'
            def basePath = Paths.get('/asdf')

        and: "being sure that it doesn't exists"
            assert !basePath.toFile().exists()

        when: 'trying to instantiate the LocalFilesystem class'
            def fs = new LocalFilesystem(basePath)

        then: 'an exception should be thrown'
            thrown FilesystemException
    }

    void 'Instantiating the LocalFilesystem class with a path pointing to a file'() {
        given: 'A base path'
            def basePath = File.createTempFile('tomb_', '_tmp').toPath()

        and: "being sure that it exists"
            assert basePath.toFile().exists()

        when: 'trying to instantiate the LocalFilesystem class'
            def fs = new LocalFilesystem(basePath)

        then: 'an exception should be thrown'
            thrown FilesystemException
    }

}

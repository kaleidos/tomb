package tomb

import spock.lang.*
import java.nio.file.Path
import java.nio.file.Paths

class LocalFilesystemMethodsSpec extends Specification {

    Path tmpPath = Paths.get(System.getProperty('java.io.tmpdir'))
    LocalFilesystem fs = new LocalFilesystem(tmpPath)

    String getRandomUUID() {
        return UUID.randomUUID().toString().replaceAll('-', '')
    }

    void 'Resolving a relative path'() {
        given: 'A relative path'
            def relativePath = Paths.get('images/relative/logo.png')

        when: 'resolving it'
            def result = fs.resolve(relativePath)

        then: 'the result should be the expected'
            result.toString() == "${tmpPath}/${relativePath}"
    }

    void 'Check if a file exists'() {
        given: 'A relative path'
            def tmpFile = File.createTempFile('tomb_', '_tmp')
            def relativePath = tmpFile.toPath()

        and: 'being sure that it exists'
            assert tmpFile.exists()

        when: 'checking if it exists'
            def result = fs.exists(relativePath)

        then: 'assert that is true'
            result

        cleanup: 'delete the generated resources'
            tmpFile.delete()
    }

    void "Check that a file doesn't exist"() {
        given: 'A relative path'
            def relativePath = Paths.get("/${randomUUID}")

        and: "being sure that it doesn't exists"
            assert !relativePath.toFile().exists()

        when: 'checking if it exists'
            def result = fs.exists(relativePath)

        then: 'assert that is true'
            !result
    }

    void 'Obtaining a file'() {
        given: 'A filename'
            def filename = randomUUID

        and: 'a file in the filesystem'
            def filePath = Paths.get("${fs.basePath}/${filename}")
            def f = filePath.toFile()
            f.text = 'holamundo'

        and: 'that exists'
            assert f.exists()

        when: 'obtaining the file'
            def result = fs.get(filePath)

        then: 'the contents of the file should be equivalent to the result'
            result.text == f.text

        cleanup: 'delete the generated resources'
            f.delete()
    }

    void "Obtaining a file that doesn't exist"() {
        given: 'A file in the filesystem'
            def f = File.createTempFile('tomb_', '_tmp')

        and: "that doesn't exist"
            f.delete()
            assert !f.exists()

        when: 'obtaining the file'
            fs.get(f.toPath())

        then: 'an exception should be thrown'
            thrown FilesystemException
    }

    void 'Obtaining a directory'() {
        given: 'A directory filesystem'
            def f = File.createTempDir('tomb_', '_tmp')

        and: 'that exist'
            assert f.exists()

        when: 'obtaining the file'
            fs.get(f.toPath())

        then: 'an exception should be thrown'
            thrown FilesystemException

        cleanup: 'delete the generated resources'
            f.delete()
    }

    void 'Uploading a file'() {
        given: 'A file'
            def f = File.createTempFile('tomb_', '_tmp')
            f.text = 'holamundo'

        and: 'a remote path'
            def relativePath = Paths.get(randomUUID)

        when: 'uploading it to the filesystem'
            fs.put(f.newInputStream(), relativePath)

        then: 'the file should be correctly uploaded'
            new File("${tmpPath}/${relativePath}").text == 'holamundo'

        cleanup: 'deleting the temporal resources created in the filesystem'
            [new File("${tmpPath}/${relativePath}"), f]*.delete()
    }

    void "Listing a remote filesystem's directory"() {
        given: 'A temporal directory'
            def tmpDir = File.createTempDir('tomb_', '_tmp')

        and: 'two temporal files in it'
            new File(tmpDir, 'file1').createNewFile()
            new File(tmpDir, 'file2').createNewFile()

        when: 'listing the temporal directory contents'
            def result = fs.list(Paths.get(tmpDir.name))

        then: 'the result should contain our two files'
            result.size() == 2
            result.every { it.startsWith('file') }

        cleanup: 'deleting the temporal resources created in the filesystem'
            tmpDir.delete()
    }

    void "Listing a remote filesystem's file"() {
        given: 'A temporal file'
            def tmpFile = File.createTempFile('tomb_', '_tmp')

        when: 'listing the temporal file contents'
            def result = fs.list(Paths.get(tmpFile.name))

        then: 'an exception should be thrown'
            thrown FilesystemException
    }

    void "Listing a remote filesystem's directory that doesn't exist"() {
        given: 'A temporal directory path'
            def tmpDirPath = Paths.get(randomUUID)

        and: "that doesn't exist"
            assert !new File("${tmpPath}/${tmpDirPath}").exists()

        when: 'listing the temporal directory contents'
            def result = fs.list(tmpDirPath)

        then: 'an exception should be thrown'
            thrown FilesystemException
    }

    void "Copying a remote filesystem's file"() {
        given: 'A temporal file'
            def tmpFile = File.createTempFile('tomb_', '_tmp')
            tmpFile.text = 'holamundo'

        and: 'a nonexistent destination path'
            def destinationPath = Paths.get(randomUUID)
            assert !destinationPath.toFile().exists()

        and: "that doesn't exist before"
            assert !destinationPath.toFile().exists()

        when: 'copying the temporal file'
            fs.copy(Paths.get(tmpFile.name), destinationPath)

        then: 'the destination path should now exist'
            new File("${tmpPath}/${destinationPath}").text == 'holamundo'

        cleanup: 'delete the generated resources'
            [tmpFile, destinationPath.toFile()]*.delete()
    }

    void "Copying a remote filesystem's file that doesn't exist"() {
        given: "A file path that doesn't exist"
            def path = Paths.get(randomUUID)
            assert !new File("${tmpPath}/${path}").exists()

        when: 'copying the temporal file'
            fs.copy(path, Paths.get(randomUUID))

        then: 'an exception should be thrown'
            thrown FilesystemException
    }

    void "Copying a remote filesystem's directory"() {
        given: 'A temporal directory'
            def tmpDir = File.createTempDir('tomb_', '_tmp')

        when: 'copying the temporal file'
            fs.copy(Paths.get(tmpDir.name), Paths.get(randomUUID))

        then: 'an exception should be thrown'
            thrown FilesystemException

        cleanup: 'delete the generated resources'
            tmpDir.delete()
    }

    void "Copying a remote filesystem's file to a destination that already exists"() {
        given: 'A temporal origin file'
            def tmpOriginFile = File.createTempFile('tomb_', '_tmp')
            tmpOriginFile.text = 'holamundo'

        and: 'A temporal destination file'
            def tmpDestinationFile = File.createTempFile('tomb_', '_tmp')
            tmpDestinationFile.text = 'holamundo'

        when: 'copying the temporal file'
            fs.copy(Paths.get(tmpOriginFile.name), Paths.get(tmpDestinationFile.name))

        then: 'an exception should be thrown'
            thrown FilesystemException

        cleanup: 'delete the generated resources'
            [tmpOriginFile, tmpDestinationFile]*.delete()
    }

    void "Moving a remote filesystem's file"() {
        given: 'A temporal file'
            def tmpFile = File.createTempFile('tomb_', '_tmp')
            tmpFile.text = 'holamundo'

        and: 'a nonexistent destination path'
            def destinationPath = Paths.get(randomUUID)

        and: "that doesn't exist before"
            assert !destinationPath.toFile().exists()

        when: 'moving the temporal file'
            fs.move(Paths.get(tmpFile.name), destinationPath)

        then: 'the destination path should now exist'
            new File("${tmpPath}/${destinationPath}").text == 'holamundo'

        and: 'the original file has been deleted'
            !tmpFile.exists()

        cleanup: 'delete the generated resources'
            destinationPath.toFile().delete()
    }

    void "Moving a remote filesystem's file that doesn't exist"() {
        given: "A file path that doesn't exist"
            def path = Paths.get(randomUUID)
            assert !new File("${tmpPath}/${path}").exists()

        when: 'moving the temporal file'
            fs.move(path, Paths.get(randomUUID))

        then: 'an exception should be thrown'
            thrown FilesystemException
    }

    void "Moving a remote filesystem's directory"() {
        given: 'A temporal directory'
            def tmpDir = File.createTempDir('tomb_', '_tmp')

        when: 'moving the temporal file'
            fs.move(Paths.get(tmpDir.name), Paths.get(randomUUID))

        then: 'an exception should be thrown'
            thrown FilesystemException

        cleanup: 'delete the generated resources'
            tmpDir.delete()
    }

    void "Moving a remote filesystem's file to a destination that already exists"() {
        given: 'A temporal origin file'
            def tmpOriginFile = File.createTempFile('tomb_', '_tmp')
            tmpOriginFile.text = 'holamundo'

        and: 'A temporal destination file'
            def tmpDestinationFile = File.createTempFile('tomb_', '_tmp')
            tmpDestinationFile.text = 'holamundo'

        when: 'moving the temporal file'
            fs.move(Paths.get(tmpOriginFile.name), Paths.get(tmpDestinationFile.name))

        then: 'an exception should be thrown'
            thrown FilesystemException

        cleanup: 'delete the generated resources'
            [tmpOriginFile, tmpDestinationFile]*.delete()
    }

}

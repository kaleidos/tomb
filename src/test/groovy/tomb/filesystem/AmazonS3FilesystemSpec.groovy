package tomb.filesystem

import spock.lang.*
import java.nio.file.Path
import java.nio.file.Paths

import tomb.exception.FilesystemException
import tomb.test.S3Configured
import tomb.Tomb

class AmazonS3FilesystemSpec extends Specification {

    Path tmpPath = Paths.get('')
    String key = System.getenv('TOMB_KEY')
    String secret = System.getenv('TOMB_SECRET')
    String bucket = System.getenv('TOMB_BUCKET')
    AmazonS3Filesystem fs = Tomb.getAmazonS3Filesystem(key, secret, bucket, tmpPath)

    String getRandomUUID() {
        return UUID.randomUUID().toString().replaceAll('-', '')
    }

    @Requires(S3Configured)
    void 'Resolving a relative path'() {
        given: 'A relative path'
            def relativePath = Paths.get('images/relative/logo.png')

        when: 'resolving it'
            def result = fs.resolve(relativePath)

        then: 'the result should be the expected'
            result.toString() == (tmpPath.toString() ? "${tmpPath}/${relativePath}" : "${relativePath}")
    }

    @Requires(S3Configured)
    void 'Check if a file exists'() {
        given: 'A relative path'
            def tmpFile = File.createTempFile('tomb_', '_tmp')
            def relativePath = Paths.get(tmpFile.name)

        and: 'uploaded to the remote filesystem'
            fs.put(tmpFile.newInputStream(), relativePath)

        when: 'checking if it exists'
            def result = fs.exists(relativePath)

        then: 'assert that is true'
            result

        cleanup: 'delete the generated resources'
            tmpFile.delete()
            fs.delete(relativePath)
    }

    @Requires(S3Configured)
    void "Check that a file doesn't exist"() {
        given: 'A relative path'
            def relativePath = Paths.get("${randomUUID}")

        when: 'checking if it exists'
            def result = fs.exists(relativePath)

        then: 'assert that is true'
            !result
    }

    @Requires(S3Configured)
    void 'Obtaining a file'() {
        given: 'A filename'
            def tmpFile = File.createTempFile('tomb_', '_tmp')
            tmpFile.text = 'holamundo'
            def relativePath = Paths.get(tmpFile.name)

        and: 'uploaded to the remote filesystem'
            fs.put(tmpFile.newInputStream(), relativePath)

        when: 'obtaining the file'
            def result = fs.get(relativePath)

        then: 'the contents of the file should be equivalent to the result'
            result.text == tmpFile.text

        cleanup: 'delete the generated resources'
            tmpFile.delete()
            fs.delete(relativePath)
    }

    @Requires(S3Configured)
    void "Obtaining a file that doesn't exist"() {
        given: 'A path in the filesystem'
            def relativePath = Paths.get("${randomUUID}")

        and: "that doesn't exist"
            assert !fs.exists(relativePath)

        when: 'obtaining the file'
            fs.get(relativePath)

        then: 'an exception should be thrown'
            thrown FilesystemException
    }

    @Requires(S3Configured)
    void 'Uploading a file'() {
        given: 'A file'
            def f = File.createTempFile('tomb_', '_tmp')
            f.text = 'holamundo'

        and: 'a remote path'
            def relativePath = Paths.get(randomUUID)

        when: 'uploading it to the filesystem'
            fs.put(f.newInputStream(), relativePath)

        then: 'the file should be correctly uploaded'
            fs.get(relativePath).text == 'holamundo'

        cleanup: 'deleting the temporal resources created in the filesystem'
            f.delete()
            fs.delete(relativePath)
    }

    @Requires(S3Configured)
    void "Listing a remote filesystem's directory"() {
        given: 'A temporal directory with two temporal files in it'
            def dirPath = Paths.get(getRandomUUID())
            def file1 = File.createTempFile('tomb_', '_tmp')
            file1.text = 'holamundo'
            def file2 = File.createTempFile('tomb_', '_tmp')
            file2.text = 'holamundo'

        and: 'uploaded to the filesystem'
            [file1,file2].each { file ->
                fs.put(file.newInputStream(), dirPath.resolve(file.name))
            }

        when: 'listing the temporal directory contents'
            def result = fs.list(dirPath)

        then: 'the result should contain our two files'
            result.size() == 2
            result.every { it.startsWith('tomb_') }

        cleanup: 'deleting the temporal resources created in the filesystem'
            [file1,file2].each { file ->
                fs.delete(Paths.get(file.name))
                file.delete()
            }
    }

    // Needs TO BE FIXED
    // The list code needs to discern between a directory and a file, asking
    // for the file type. At this point, trying to list a file will return an
    // empty list, and it needs to throw an exception
    @Ignore
    @Requires(S3Configured)
    void "Listing a remote filesystem's file"() {
        given: 'A temporal file'
            def tmpFile = File.createTempFile('tomb_', '_tmp')
            tmpFile.text = 'holamundo'

        and: 'uploaded to the remote filesystem'
            def relativePath = Paths.get(tmpFile.name)
            fs.put(tmpFile.newInputStream(), relativePath)

        when: 'listing the temporal file contents'
            def result = fs.list(Paths.get(tmpFile.name))

        then: 'an exception should be thrown'
            thrown FilesystemException
    }

    @Requires(S3Configured)
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

    @Requires(S3Configured)
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

    @Requires(S3Configured)
    void "Copying a remote filesystem's file that doesn't exist"() {
        given: "A file path that doesn't exist"
            def path = Paths.get(randomUUID)
            assert !new File("${tmpPath}/${path}").exists()

        when: 'copying the temporal file'
            fs.copy(path, Paths.get(randomUUID))

        then: 'an exception should be thrown'
            thrown FilesystemException
    }

    @Requires(S3Configured)
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

    @Requires(S3Configured)
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

    @Requires(S3Configured)
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

    @Requires(S3Configured)
    void "Moving a remote filesystem's file that doesn't exist"() {
        given: "A file path that doesn't exist"
            def path = Paths.get(randomUUID)
            assert !new File("${tmpPath}/${path}").exists()

        when: 'moving the temporal file'
            fs.move(path, Paths.get(randomUUID))

        then: 'an exception should be thrown'
            thrown FilesystemException
    }

    @Requires(S3Configured)
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

    @Requires(S3Configured)
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

    @Requires(S3Configured)
    void "Deleting a file"() {
        given: 'A file'
            def tmpFile = File.createTempFile('tomb_', '_tmp')
            tmpFile.text = 'holamundo'
            assert tmpFile.exists()

        when: 'deleting the file'
            fs.delete(tmpFile.toPath())

        then: 'the file should be deleted'
            !tmpFile.exists()
    }

    @Requires(S3Configured)
    void "Deleting a nonexistent"() {
        given: 'A path to a file'
            def tmpFilePath = Paths.get(randomUUID)

        and: "that doesn't exists"
            assert !tmpFilePath.toFile().exists()

        when: 'trying to delete it'
            fs.delete(tmpFilePath)

        then: 'an exception should be thrown'
            thrown FilesystemException
    }

    @Requires(S3Configured)
    void "Deleting a directory"() {
        given: 'A directory'
            def tmpDir = File.createTempDir('tomb_', '_tmp')

        and: 'that exists'
            assert tmpDir.exists()

        when: 'trying to delete it'
            fs.delete(Paths.get(tmpDir.name))

        then: 'an exception should be thrown'
            thrown FilesystemException

        cleanup: 'delete the generated resources'
            tmpDir.delete()
    }

    @Requires(S3Configured)
    void "Obtaining the URI of a file"() {
        given: 'A file'
            def tmpFile = File.createTempFile('tomb_', '_tmp')
            tmpFile.text = 'holamundo'
            assert tmpFile.exists()

        when: 'obtaining its URI'
            def result = fs.getUri(Paths.get(tmpFile.name))

        then: 'the URI should be formatted as expected'
            result.toString() == "file:${tmpFile}"
    }

    @Requires(S3Configured)
    void "Obtaining the URI of a nonexistent file"() {
        given: 'A path to a file'
            def tmpFilePath = Paths.get(randomUUID)

        and: "that doesn't exists"
            assert !tmpFilePath.toFile().exists()

        when: 'trying to delete it'
            fs.getUri(tmpFilePath)

        then: 'an exception should be thrown'
            thrown FilesystemException
    }

    @Requires(S3Configured)
    void "Obtaining the URI of a directory"() {
        given: 'A directory'
            def tmpDir = File.createTempDir('tomb_', '_tmp')
            assert tmpDir.exists()

        when: 'obtaining its URI'
            def result = fs.getUri(Paths.get(tmpDir.name))

        then: 'the URI should be formatted as expected'
            result.toString() == "file:${tmpDir}/"
    }

}

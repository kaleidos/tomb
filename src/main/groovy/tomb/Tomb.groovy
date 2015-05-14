package tomb

import java.nio.file.Path
import java.nio.file.Paths

import com.amazonaws.services.s3.AmazonS3Client

import tomb.utils.AmazonS3Utils
import tomb.filesystem.LocalFilesystem
import tomb.filesystem.AmazonS3Filesystem
import tomb.filesystem.FilesystemProvider
import tomb.exception.FilesystemException

class Tomb {

    private static userValueOrDefault(String userValue, String defaultValue) {
        userValue != null ? userValue : defaultValue
    }

    /**
     * Returns the provider defined at config dictionary.
     * @param config Configuration for the filesystem. It contains the filesystem
     *               configuration. This configuration depends on the underliying
     *               filesystem.
     * @param filesystem The filesystem to be instantiated
     *
     * @return The instance to the filesystem.
     */
    static FilesystemProvider getFilesystem(Map config, String filesystem) {
        switch(filesystem) {
            case 'S3':
                return getAmazonS3Filesystem(config.key, config.secret, config.bucket, Paths.get(userValueOrDefault(config.basePath, '')))
            case 'local':
                return getLocalFilesystem(Paths.get(userValueOrDefault(config.basePath, '/')))
            default:
                throw new FilesystemException("Unknown filesystem ${filesystem}");
        }
    }

    static FilesystemProvider getLocalFilesystem(Path basePath = Paths.get('/')) {
        File basePathFile = basePath.toFile()
        if (!basePathFile.exists()) {
            throw new FilesystemException("Path ${basePath} doesn't exist")
        }

        if (!basePathFile.isDirectory()) {
            throw new FilesystemException("Path ${basePath} is not a directory")
        }

        LocalFilesystem fs = new LocalFilesystem()
        fs.basePath = basePath

        return fs
    }

    static FilesystemProvider getAmazonS3Filesystem(String key, String secret, String bucket, Path basePath = Paths.get('')) {
        AmazonS3Client s3Client = AmazonS3Utils.getClient(key, secret)
        AmazonS3Utils.checkBucket(s3Client, bucket)

        AmazonS3Filesystem fs = new AmazonS3Filesystem()
        fs.basePath = basePath
        fs.s3Client = s3Client
        fs.bucket = bucket

        return fs
    }

}

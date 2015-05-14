package tomb.filesystem

import java.nio.file.Path
import java.nio.file.Paths

import tomb.exception.FilesystemException

class LocalFilesystem implements FilesystemProvider {

    Path basePath

    Path resolve(Path relativePath) {
        return basePath.resolve(relativePath)
    }

    Boolean exists(Path relativePath) {
        Path path = resolve(relativePath)

        return path.toFile().exists()
    }

    InputStream get(Path relativePath) {
        Path path = resolve(relativePath)
        File file = path.toFile()

        if (!file.exists()) {
            throw new FilesystemException("File ${path} doesn't exist")
        }

        if (!file.isFile()) {
            throw new FilesystemException("Path ${path} is not a file")
        }

        return file.newInputStream()
    }

    void put(InputStream inputStream, Path relativePath) {
        Path path = resolve(relativePath)
        File file = path.toFile()

        try {
            file.withOutputStream { it.write(inputStream.bytes) }
        } catch (Exception e) {
            throw new FilesystemException("Unable to write file ${path}")
        }
    }

    List<String> list(Path relativePath = Paths.get('')) {
        Path path = resolve(relativePath)
        File file = path.toFile()

        if (!file.exists()) {
            throw new FilesystemException("Directory ${path} doesn't exist")
        }

        if (!file.isDirectory()) {
            throw new FilesystemException("Path ${path} is not a directory")
        }

        return file.list()
    }

    void copy(Path initialRelativePath, Path destinationRelativePath) {
        Path initialPath = resolve(initialRelativePath)
        File initialFile = initialPath.toFile()

        if (!initialFile.exists()) {
            throw new FilesystemException("File ${initialPath} doesn't exist")
        }

        if (!initialFile.isFile()) {
            throw new FilesystemException("Path ${initialPath} is not a file")
        }

        Path destinationPath = resolve(destinationRelativePath)
        File destinationFile = destinationPath.toFile()

        if (destinationFile.exists()) {
            throw new FilesystemException("File ${destinationPath} already exist")
        }

        destinationFile.withOutputStream { it.write(initialFile.bytes) }
    }

    void move(Path initialRelativePath, Path destinationRelativePath) {
        Path initialPath = resolve(initialRelativePath)
        File initialFile = initialPath.toFile()

        if (!initialFile.exists()) {
            throw new FilesystemException("File ${initialPath} doesn't exist")
        }

        if (!initialFile.isFile()) {
            throw new FilesystemException("Path ${initialPath} is not a file")
        }

        Path destinationPath = resolve(destinationRelativePath)
        File destinationFile = destinationPath.toFile()

        if (destinationFile.exists()) {
            throw new FilesystemException("File ${destinationPath} already exist")
        }

        destinationFile.withOutputStream { it.write(initialFile.bytes) }
        initialFile.delete()
    }

    void delete(Path relativePath) {
        File file = relativePath.toFile()

        if (!file.exists()) {
            throw new FilesystemException("File ${relativePath} doesn't exist")
        }

        if (!file.isFile()) {
            throw new FilesystemException("Path ${relativePath} is not a file")
        }

        file.delete()
    }

    URI getUri(Path relativePath) {
        Path path = resolve(relativePath)
        File file = path.toFile()

        if (!file.exists()) {
            throw new FilesystemException("File ${relativePath} doesn't exist")
        }

        return file.toURI()
    }

}

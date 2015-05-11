package tomb

import java.nio.file.Path

interface FilesystemProvider {

    Path resolve(Path relativePath)

    Boolean exists(Path relativePath)

    InputStream get(Path relativePath)

    void put(InputStream inputStream, Path relativePath)

    List<String> list(Path relativePath)

    void copy(Path initialRelativePath, Path destinationRelativePath)

    void move(Path initialRelativePath, Path destinationRelativePath)

    void delete(Path relativePath)

    URI getUri(Path relativePath)

}

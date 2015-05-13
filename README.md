# Tomb
[![Build Status](https://travis-ci.org/kaleidos/tomb.svg?branch=master)](https://travis-ci.org/kaleidos/tomb)

Tomb is a simple groovy module to abstract the usage of different filesystems.

http://kaleidos.github.io/tomb

## Develop

The integration tests related to the `AmazonS3` backend need three environment variables to be set:

* `TOMB_KEY`: The amazon S3 key
* `TOMB_SECRET`: The amazon S3 secret
* `TOMB_BUCKET`: The amazon S3 bucket that we will use to run the tests

You can create a simple script file to run the `test` task with those variables set:

```bash
#!/bin/bash
export TOMB_KEY='my key'
export TOMB_SECRET='my secret'
export TOMB_BUCKET='test-bucket'

./gradlew --daemon test
```

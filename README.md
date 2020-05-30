# Ignite OOM issue

to reproduce:

```
$ gradlew run
```

Ignite will hang on `Random2LruPageEvictionTracker`

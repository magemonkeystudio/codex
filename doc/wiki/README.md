# Wiki staging copy

These files are a **staging copy** of the Codex wiki pages, placed here only because the session that
wrote them could not push to `codex.wiki.git` directly.

They are already committed in the wiki repository's own history as a single commit. To publish them:

```sh
git clone https://github.com/magemonkeystudio/codex.wiki.git
cd codex.wiki
cp ../codex/doc/wiki/*.md .
rm README.md
git add -A
git commit -m "Expand wiki from single page to full documentation set"
git push origin master
```

Or, using the bundle produced alongside these files, which preserves the original commit:

```sh
git clone https://github.com/magemonkeystudio/codex.wiki.git
cd codex.wiki
git pull /path/to/codex-wiki.bundle master
git push origin master
```

Once the wiki is updated, **delete this directory** — the wiki is the source of truth, and leaving a
second copy here will drift.

```sh
git rm -r doc/wiki
```

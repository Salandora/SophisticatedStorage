# Changelog

### General
- Update to version 0.10.26
- Code cleanup
- Remove custom ItemStackHandler implementation

### Fixes
- Try to fix a crash with compression upgrade and e.g. hopper, caused by a simulate extraction getting rolled back.
- Sorting keybind was not working with keys only with mouse buttons
- Fix quilt loader crashing due to wrong version predicate parser being used
- Give the generated recipes unique ids. This fixes errors thrown by EMI complaining about registering the same recipe id multiple times.
- Fixes a dupe bug in combination with beans backpack in limited barrels
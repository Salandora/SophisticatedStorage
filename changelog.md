# Changelog

### General
- Added fabric version of the logo and used it in `fabric.mod.json`.

### Improvements
- Changed the way Litematica support works by adding the trinket slots as a container in the `getInventoryItemCounts` function.
- Moved Litematica support to `SophisticatedCore`.
- Moved Storage Wrappers to `SophisticatedCore` for better compatibility between sophisticated mods. *(Reverted later)*

### Fixes
- Fixed chipped compatibility.
- Fixed a crash when trying to take items from a limited barrel in a server environment. Fixes [Salandora/SophisticatedStorage#3](https://github.com/Salandora/SophisticatedStorage/issues/3).
- Fixed a crash when placing Storage Link in a server environment.
- Reworked Litematica compatibility due to reverting the Storage Wrapper move commits.
- Fixed server incompatibility with reworked Litematica compatibility.
- Completely reworked Litematica compatibility.
- Disabled `isSameThread` requirement as it stopped the StorageWrapper from getting the right data.
- Fixed controller not inserting items into storages. Fixes [Salandora/SophisticatedStorage#1](https://github.com/Salandora/SophisticatedStorage/issues/1).
- Properly implemented `getSlot` in `ControllerBlockEntityBase`.

### Data Generation
- Regenerated data.
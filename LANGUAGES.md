# TriggerVolumes - Multi-Language Support

This plugin supports multiple languages. The language can be changed in the `config.yml` file.

## Available Languages

- **en** - English (Default)
- **de** - German (Deutsch)
- **fr** - French (Français)
- **pl** - Polish (Polski)
- **ru** - Russian (Русский)
- **ja** - Japanese (日本語)
- **es** - Spanish (Español)

## Changing the Language

1. Open `config.yml` in your plugin folder
2. Find the `language` section:
```yaml
language:
  default: en
```
3. Change `en` to your desired language code (e.g., `de` for German)
4. Run `/trigger reload` or restart the server

## Adding Custom Translations

You can edit the language files in the `lang/` folder to customize messages:

- `lang/en.yml` - English
- `lang/de.yml` - German
- `lang/fr.yml` - French
- `lang/pl.yml` - Polish
- `lang/ru.yml` - Russian
- `lang/ja.yml` - Japanese
- `lang/es.yml` - Spanish

After editing a language file, use `/trigger reload` to apply changes.

## Language File Format

Each language file contains all plugin messages:

```yaml
prefix: "&8[&6TriggerVolumes&8] "
no-permission: "&cYou don't have permission to do this!"
pos1-set: "&aPosition 1 set at &e%x%, %y%, %z%"
# ... more messages
```

### Placeholders

Some messages support placeholders that are replaced with dynamic values:

- `%x%`, `%y%`, `%z%` - Coordinates
- `%name%` - Volume name
- `%player%` - Player name

## Reload Command

The plugin includes a reload command to apply changes without restarting:

```
/trigger reload
```

This command:
- Reloads the main configuration
- Reloads all language files
- Reloads all trigger volumes
- Requires `triggervolumes.admin` permission

## Creating a New Language

To add a new language:

1. Copy one of the existing language files (e.g., `en.yml`)
2. Name it with your language code (e.g., `it.yml` for Italian)
3. Translate all messages in the file
4. The plugin will automatically detect and load the new language
5. Update the LanguageManager class to include the new language code in the `availableLanguages` array

## Support

For questions or issues with translations, please open an issue on GitHub.

---

**Author:** zfzfg  
**Plugin Version:** 1.0.0

export class ConfigData {

  config: Record<
    string,
    string | boolean | null
  > = {};

  constructor(
    config: Record<
      string,
      string | null | undefined
    >
  ) {
    Object.keys(config).forEach(key => {
      const value = config[key];

      if (
        value === '' || value === null || value === undefined
      ) {
        this.config[key] = null;
      } else if (value === 'true') {
        this.config[key] = true;
      } else if (value === 'false') {
        this.config[key] = false;
      } else {
        this.config[key] = value;
      }
    });
  }
}
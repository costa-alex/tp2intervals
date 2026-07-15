export class TrainingTypes {
  static trainingTypes = [
    {title: "Ride", value: "BIKE"},
    {title: "MTB", value: "MTB"},
    {title: "Virtual Bike", value: "VIRTUAL_BIKE"},
    {title: "Run", value: "RUN"},
    {title: "Swim", value: "SWIM"},
    {title: "Walk", value: "WALK"},
    {title: "Strength Training", value: "STRENGTH"},
    {title: "Any other", value: "UNKNOWN"},
  ]

  static getTitle(value: string) {
    return this.trainingTypes.find(type => type.value === value)?.title
  }
}

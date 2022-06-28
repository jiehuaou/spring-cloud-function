package uppercase;

public class Input {

  private String input;

  private String output;

  public Input() {
  }

  public String getInput() {
    return input;
  }

  public void setInput(String input) {
    this.input = input;
  }

  @Override
  public String toString() {
    return "Input{" +
        "input='" + input + '\'' +
        '}';
  }

  public String getOutput() {
    return output;
  }

  public void setOutput(String output) {
    this.output = output;
  }
}

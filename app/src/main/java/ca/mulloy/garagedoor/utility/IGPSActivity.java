package ca.mulloy.garagedoor.utility;

public interface IGPSActivity {
    public void locationChanged(double longitude, double latitude);
    public void displayGPSSettingsDialog();
}

package com.pythonforge.model;

import java.nio.file.Path;
import java.util.Objects;

/**
 * Python 环境信息。
 */
public final class PythonEnvironment {

    private final Path executable;

    private final String version;

    private final PythonArchitecture architecture;

    private final Path pipExecutable;

    private final Path sitePackages;

    private final boolean pyInstallerAvailable;

    private final String pyInstallerVersion;

    public PythonEnvironment(
            Path executable,
            String version,
            PythonArchitecture architecture,
            Path pipExecutable,
            Path sitePackages,
            boolean pyInstallerAvailable,
            String pyInstallerVersion) {

        this.executable =
                Objects.requireNonNull(executable);

        this.version =
                Objects.requireNonNull(version);

        this.architecture =
                Objects.requireNonNull(architecture);

        this.pipExecutable =
                pipExecutable;

        this.sitePackages =
                sitePackages;

        this.pyInstallerAvailable =
                pyInstallerAvailable;

        this.pyInstallerVersion =
                pyInstallerVersion;
    }

    public Path getExecutable() {
        return executable;
    }

    public String getVersion() {
        return version;
    }

    public PythonArchitecture getArchitecture() {
        return architecture;
    }

    public Path getPipExecutable() {
        return pipExecutable;
    }

    public Path getSitePackages() {
        return sitePackages;
    }

    public boolean isPyInstallerAvailable() {
        return pyInstallerAvailable;
    }

    public String getPyInstallerVersion() {
        return pyInstallerVersion;
    }

    public boolean isPipAvailable() {
        return pipExecutable != null;
    }

    public String getExecutableString() {
        return executable.toString();
    }

    public String getPipStatus() {

        return isPipAvailable()
                ? "Available"
                : "Not Available";
    }

    public String getPyInstallerStatus() {

        if (!pyInstallerAvailable) {
            return "Not Installed";
        }

        if (pyInstallerVersion == null
                || pyInstallerVersion.isBlank()) {

            return "Available";
        }

        return "Available "
                + pyInstallerVersion;
    }

    @Override
    public boolean equals(
            Object object) {

        if (this == object) {
            return true;
        }

        if (!(object
                instanceof PythonEnvironment other)) {

            return false;
        }

        return executable
                .toAbsolutePath()
                .normalize()
                .equals(
                        other.executable
                                .toAbsolutePath()
                                .normalize()
                );
    }

    @Override
    public int hashCode() {

        return executable
                .toAbsolutePath()
                .normalize()
                .hashCode();
    }

    @Override
    public String toString() {

        return "PythonEnvironment{" +
                "executable=" + executable +
                ", version='" + version + '\'' +
                ", architecture=" + architecture +
                ", pipExecutable=" + pipExecutable +
                ", sitePackages=" + sitePackages +
                ", pyInstallerAvailable=" +
                pyInstallerAvailable +
                ", pyInstallerVersion='" +
                pyInstallerVersion + '\'' +
                '}';
    }
}
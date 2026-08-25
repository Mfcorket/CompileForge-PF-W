package com.pythonforge.model;

/**
 * Python运行架构。
 */
public enum PythonArchitecture {

    X86("x86", 32),

    X64("x64", 64),

    ARM64("ARM64", 64),

    UNKNOWN("Unknown", 0);

    private final String name;
    private final int bits;

    PythonArchitecture(String name, int bits) {
        this.name = name;
        this.bits = bits;
    }

    public String getName() {
        return name;
    }

    public int getBits() {
        return bits;
    }

    @Override
    public String toString() {
        return name;
    }
}


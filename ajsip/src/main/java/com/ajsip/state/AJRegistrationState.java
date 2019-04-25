package com.ajsip.state;

public enum AJRegistrationState {
        AJRegistration_None(0),
        AJRegistration_Progress(1),
        AJRegistration_Ok(2),
        AJRegistration_Cleared(3),
        AJRegistration_Failed(4);

        protected final int mValue;

        private AJRegistrationState(int value) {
            this.mValue = value;
        }

        public static AJRegistrationState fromInt(int value) throws RuntimeException {
            switch(value) {
                case 0:
                    return AJRegistration_None;
                case 1:
                    return AJRegistration_Progress;
                case 2:
                    return AJRegistration_Ok;
                case 3:
                    return AJRegistration_Cleared;
                case 4:
                    return AJRegistration_Failed;
                default:
                    throw new RuntimeException("Unhandled enum value " + value + " for RegistrationState");
            }
        }

        public int toInt() {
            return this.mValue;
        }
    }

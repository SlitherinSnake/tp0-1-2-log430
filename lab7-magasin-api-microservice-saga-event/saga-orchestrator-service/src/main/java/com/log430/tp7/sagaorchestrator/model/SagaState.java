package com.log430.tp7.sagaorchestrator.model;

import java.util.EnumSet;
import java.util.Set;

/**
 * Enumeration representing all possible states in the saga state machine.
 * Each state defines valid transitions to maintain state machine integrity.
 */
public enum SagaState {
    
    /**
     * Initial state when a sale request is received.
     * Next states: STOCK_VERIFYING, SALE_FAILED
     */
    SALE_INITIATED {
        @Override
        public Set<SagaState> getValidTransitions() {
            return EnumSet.of(STOCK_VERIFYING, SALE_FAILED);
        }
        
        @Override
        public boolean isInitialState() {
            return true;
        }
    },
    
    /**
     * State when verifying stock availability with inventory service.
     * Next states: STOCK_RESERVING, SALE_FAILED
     */
    STOCK_VERIFYING {
        @Override
        public Set<SagaState> getValidTransitions() {
            return EnumSet.of(STOCK_RESERVING, SALE_FAILED);
        }
    },
    
    /**
     * State when reserving stock in inventory service.
     * Next states: PAYMENT_PROCESSING, SALE_FAILED
     */
    STOCK_RESERVING {
        @Override
        public Set<SagaState> getValidTransitions() {
            return EnumSet.of(PAYMENT_PROCESSING, SALE_FAILED);
        }
    },
    
    /**
     * State when processing payment through transaction service.
     * Next states: ORDER_CONFIRMING, STOCK_RELEASING
     */
    PAYMENT_PROCESSING {
        @Override
        public Set<SagaState> getValidTransitions() {
            return EnumSet.of(ORDER_CONFIRMING, STOCK_RELEASING);
        }
    },
    
    /**
     * State when confirming order creation with store service.
     * Next states: SALE_CONFIRMED, STOCK_RELEASING
     */
    ORDER_CONFIRMING {
        @Override
        public Set<SagaState> getValidTransitions() {
            return EnumSet.of(SALE_CONFIRMED, STOCK_RELEASING);
        }
    },
    
    /**
     * Compensation state when releasing reserved stock due to failures.
     * Next states: SALE_FAILED
     */
    STOCK_RELEASING {
        @Override
        public Set<SagaState> getValidTransitions() {
            return EnumSet.of(SALE_FAILED);
        }
        
        @Override
        public boolean isCompensationState() {
            return true;
        }
    },
    
    /**
     * Final successful state when the entire saga completes successfully.
     * No further transitions allowed.
     */
    SALE_CONFIRMED {
        @Override
        public Set<SagaState> getValidTransitions() {
            return EnumSet.noneOf(SagaState.class);
        }
        
        @Override
        public boolean isFinalState() {
            return true;
        }
        
        @Override
        public boolean isSuccessState() {
            return true;
        }
    },
    
    /**
     * Final failure state when the saga fails or compensation completes.
     * No further transitions allowed.
     */
    SALE_FAILED {
        @Override
        public Set<SagaState> getValidTransitions() {
            return EnumSet.noneOf(SagaState.class);
        }
        
        @Override
        public boolean isFinalState() {
            return true;
        }
        
        @Override
        public boolean isFailureState() {
            return true;
        }
    };
    
    /**
     * Returns the set of valid states this state can transition to.
     * 
     * @return set of valid next states
     */
    public abstract Set<SagaState> getValidTransitions();
    
    /**
     * Checks if this state can transition to the specified target state.
     * 
     * @param targetState the state to transition to
     * @return true if the transition is valid
     */
    public boolean canTransitionTo(SagaState targetState) {
        return getValidTransitions().contains(targetState);
    }
    
    /**
     * Checks if this is an initial state of the saga.
     * 
     * @return true if this is an initial state
     */
    public boolean isInitialState() {
        return false;
    }
    
    /**
     * Checks if this is a final state (no further transitions possible).
     * 
     * @return true if this is a final state
     */
    public boolean isFinalState() {
        return false;
    }
    
    /**
     * Checks if this is a successful final state.
     * 
     * @return true if this represents successful completion
     */
    public boolean isSuccessState() {
        return false;
    }
    
    /**
     * Checks if this is a failure final state.
     * 
     * @return true if this represents failure
     */
    public boolean isFailureState() {
        return false;
    }
    
    /**
     * Checks if this is a compensation state (rollback operation).
     * 
     * @return true if this is a compensation state
     */
    public boolean isCompensationState() {
        return false;
    }
    
    /**
     * Checks if this state represents an active (non-final) saga.
     * 
     * @return true if the saga is still active
     */
    public boolean isActiveState() {
        return !isFinalState();
    }
    
    /**
     * Gets the next state in the happy path flow.
     * Returns null for final states or compensation states.
     * 
     * @return the next state in normal flow, or null if not applicable
     */
    public SagaState getNextHappyPathState() {
        return switch (this) {
            case SALE_INITIATED -> STOCK_VERIFYING;
            case STOCK_VERIFYING -> STOCK_RESERVING;
            case STOCK_RESERVING -> PAYMENT_PROCESSING;
            case PAYMENT_PROCESSING -> ORDER_CONFIRMING;
            case ORDER_CONFIRMING -> SALE_CONFIRMED;
            default -> null;
        };
    }
    
    /**
     * Gets the compensation state for this state when failure occurs.
     * Returns null if no compensation is needed.
     * 
     * @return the compensation state, or null if not applicable
     */
    public SagaState getCompensationState() {
        return switch (this) {
            case PAYMENT_PROCESSING, ORDER_CONFIRMING -> STOCK_RELEASING;
            default -> SALE_FAILED;
        };
    }
    
    /**
     * Returns all initial states of the saga state machine.
     * 
     * @return set of initial states
     */
    public static Set<SagaState> getInitialStates() {
        return EnumSet.of(SALE_INITIATED);
    }
    
    /**
     * Returns all final states of the saga state machine.
     * 
     * @return set of final states
     */
    public static Set<SagaState> getFinalStates() {
        return EnumSet.of(SALE_CONFIRMED, SALE_FAILED);
    }
    
    /**
     * Returns all compensation states of the saga state machine.
     * 
     * @return set of compensation states
     */
    public static Set<SagaState> getCompensationStates() {
        return EnumSet.of(STOCK_RELEASING);
    }
    
    /**
     * Returns all active (non-final) states of the saga state machine.
     * 
     * @return set of active states
     */
    public static Set<SagaState> getActiveStates() {
        return EnumSet.of(SALE_INITIATED, STOCK_VERIFYING, STOCK_RESERVING, 
                         PAYMENT_PROCESSING, ORDER_CONFIRMING, STOCK_RELEASING);
    }
    
    /**
     * Validates the entire state machine for consistency.
     * Ensures all states have valid transitions and no orphaned states exist.
     * 
     * @return true if the state machine is valid
     */
    public static boolean validateStateMachine() {
        // Check that all states are reachable
        Set<SagaState> reachableStates = EnumSet.noneOf(SagaState.class);
        Set<SagaState> toProcess = EnumSet.copyOf(getInitialStates());
        
        while (!toProcess.isEmpty()) {
            SagaState current = toProcess.iterator().next();
            toProcess.remove(current);
            
            if (!reachableStates.contains(current)) {
                reachableStates.add(current);
                toProcess.addAll(current.getValidTransitions());
            }
        }
        
        // All states should be reachable
        return reachableStates.size() == SagaState.values().length;
    }
}
# Account Action Callout Implementation

This implementation adds a modern Compose version of the account action callout dialog that appears when long-clicking an account item in the account list.

## Overview

The callout recreates the functionality of the legacy `QuickActionGrid` with:
- 3x3 grid layout of action buttons  
- Callout-style popup with arrow pointing to selected account
- Modern Material Design styling
- Same actions as original implementation

## Components Added

### 1. AccountActionCallout.kt
- Main callout composable with popup positioning
- 3x3 grid layout of action buttons
- Arrow pointing down to selected account
- Material Design styling with shadows and rounded corners

### 2. AccountActionCalloutItem.kt
- Data models for callout actions (`AccountActionCalloutItem`, `AccountAction`)
- Enum defining all available account actions

### 3. AccountActionCalloutUtils.kt  
- Utility to create action items based on account state
- Maintains same logic as legacy `prepareAccountActionGrid()`
- Handles active/inactive account states

## Resources Copied

### Icons (copied from main app to feature module)
- `ic_action_info.png` - Account info
- `ic_action_list.png` - Blotter/transactions 
- `ic_action_edit.png` - Edit account
- `ic_action_add.png` - Add transaction
- `ic_action_transfer.png` - Add transfer
- `ic_action_tick.png` - Update balance
- `ic_action_flash.png` - Delete old transactions
- `ic_action_lock_closed.png` - Close account
- `ic_action_lock_open.png` - Reopen account  
- `ic_action_trash.png` - Delete account

### Strings (added to feature module)
- All action titles from main app strings.xml

## Integration

### Modified Files
- `AccountListScreen.kt` - Added callout state management and action handling
- `AccountList.kt` - Updated to support callout positioning parameters
- `AccountListItem.kt` - Updated for callout interaction (using simplified approach)

### Usage
The callout automatically appears when long-clicking any account item in the AccountListScreen. Actions are handled through the existing navigation callbacks.

## Action Mapping

The callout maintains the same 9 actions as the legacy implementation:

1. **Info** → Navigate to account details
2. **Blotter** → Navigate to account transactions
3. **Edit** → Navigate to edit account
4. **Transaction** → Navigate to add transaction  
5. **Transfer** → Navigate to add transfer
6. **Balance** → Navigate to update balance
7. **Delete old transactions** → Navigate to purge account
8. **Close/Reopen account** → Toggle account status (TODO)
9. **Delete account** → Delete account with confirmation (TODO)

## Demo

The implementation can be tested using the existing `AccountListComposeActivity` in the modern-app module. Long-click any account to see the callout in action.

## Future Improvements

1. **Accurate positioning** - Currently uses dummy coordinates. Should capture actual touch/click bounds.
2. **Animation** - Add entrance/exit animations for the callout.
3. **Dynamic grid sizing** - Support different grid sizes based on action count.
4. **Close/Delete actions** - Complete implementation of account close/reopen and delete with confirmation dialogs.
5. **String resources** - Replace hardcoded strings with proper string resource references.
6. **Accessibility** - Add proper accessibility labels and navigation.

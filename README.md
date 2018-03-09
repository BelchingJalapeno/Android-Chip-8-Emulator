# Android Chip-8 emulator
A Chip-8 emulator for Android with a built in psudo-assembly language and editor for creating your own
chip-8 programs. [Click for more info on the Chip-8 system.](https://en.wikipedia.org/wiki/CHIP-8)

# Installing
Requires at least JDK 6, Android sdk 21, and Android sdk build tools 26.0.2 installed to build, with an Android device connected with developer options
turned on.  After those are installed and your device setup, navigate to the root directory and run `gradlew installDebug` to build and install.
# Registers
The Chip-8 has 19 registers, 16 general purpose(with one also acting as a flag for certain opcodes), and 3 special.
## General
The Chip-8 has 16 general purpose 8-bit registers labeled `V0` to `VF`. All instructions use these registers except for
`JMP`, `CLS`, `CAL`, and `RET`.

The `VF` register doubles as a carry flag which modifies its value due to some instructions. The instructions that modify the `VF`
register are `ADD` (*when adding 2 registers*) ,`DRW`, `SHL`, `SHR`, `SUB`, and `SUBY`.
## Special
The Chip-8 also contains 3 special purpose registers, `I`, `DT`, and `ST`.

The `I` register is a 16-bit register (with only the last 12 bits used) that stores memory locations. It can be set
using `ADD` and `MOV`. Its used in memory manipulation commands which are `JMP`, `CAL`, `BCD`, `STR`, and `LOD`.

It also contains 2 8-bit timer registers, `DT` and `ST`, which count down at a rate of 60hz. When these timers reach
`0`, they stay at `0` until the timer is set to a different value. If the value of `ST` is greater than `0`, it
will produce a beep sound. Both registers can only be written to using `MOV VX ST`, with `VX` being one of the general
purpose registers. `DT` **can** be read from but **not** `ST`. Use `MOV DT VX` to read from `DT`.
# Opcodes

`MOV dst src` - sets the dst register to src or src register

`ADD dst src` - adds the src or src register to dst register, setting the carry flag if adding a register and there is a carry

`JMP destination` - goto the destination label

`DRW x y height` - draw a sprite to screen at x, y using the reading height bytes from the location of the `I` register

`CLS` - clears the screen, turning all pixels off

`CAL` - calls a subroutine

`RET` - returns from a subroutine

`EQ dst src` - skips the next instruction if the dst register and src or src register are equal

`NEQ dst src` - skips the next instruction if the dst register and src or src register are not equal

`AND dst src` - sets dst to dst AND src 

`OR dst src` - sets dst to dst OR src 

`XOR dst src` - sets dst to dst XOR src 

`SHL dst src` - shift the bits left, VF is set to the most significant bit of VX before the shift

`SHR dst src` - shift the bits right, VF is set to the least significant bit of VX before the shift

`RND dst value` sets dst to a random number & a value

`KP reg` - skips the next instruction if the key stored in register is pressed

`KNP reg` - skips the next instruction if the key stored in register is NOT pressed

`KW reg` - waits for a key to be pressed, then storing the key in the register

`BCD reg` - stores the value of the register at addresses I, I+1, and I+2 as the hundreds, tens, and ones

`STR VX` - stores registers V0 - VX in memory starting at address I

`LOD VX` - fills registers V0 - VX with the values from memory starting at address I

`SUB dst src` - subtracts dst from src registers and sets the value to dst and sets carry flag(VF)

`SUBY dst src` - subtracts src from dst registers and sets the value to dst and sets carry flag(VF)

`JMP0 destination` - goto the destination label plus V0

Labels start with `.` followed by their name. Comments start with a `;`
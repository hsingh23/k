require "seq.k"
require "if.k"

module WHILE-SYNTAX 
  imports SEQ-SYNTAX
  imports IF-SYNTAX
  syntax Exp ::= "while" Exp "do" Exp [while]
endmodule
  
module WHILE 
  imports WHILE-SYNTAX
  imports SEQ
  imports IF

  rule while E do S => if E then (S ; while E do S) else skip 
endmodule

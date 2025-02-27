//===-- vfp_alias.S - Override generic runtimes with VFP version ----------===//
//
//                     The LLVM Compiler Infrastructure
//
// This file is dual licensed under the MIT and the University of Illinois Open
// Source Licenses. See LICENSE.TXT for details.
//
//===----------------------------------------------------------------------===//

#include "../assembly.h"

#include "adddf3vfp.S"
#include "addsf3vfp.S"
#include "divdf3vfp.S"
#include "divsf3vfp.S"
#include "extendsfdf2vfp.S"
#include "fixdfsivfp.S"
#include "fixsfsivfp.S"
#include "fixunsdfsivfp.S"
#include "fixunssfsivfp.S"
#include "floatsidfvfp.S"
#include "floatsisfvfp.S"
#include "floatunssidfvfp.S"
#include "floatunssisfvfp.S"
#include "muldf3vfp.S"
#include "mulsf3vfp.S"
#include "negdf2vfp.S"
#include "negsf2vfp.S"
#include "subdf3vfp.S"
#include "subsf3vfp.S"
#include "truncdfsf2vfp.S"
#include "unorddf2vfp.S"
#include "unordsf2vfp.S"

DEFINE_AEABI_FUNCTION_ALIAS(__aeabi_dadd, __adddf3)
DEFINE_AEABI_FUNCTION_ALIAS(__aeabi_fadd, __addsf3)
DEFINE_AEABI_FUNCTION_ALIAS(__aeabi_ddiv, __divdf3)
DEFINE_AEABI_FUNCTION_ALIAS(__aeabi_fdiv, __divsf3)
DEFINE_AEABI_FUNCTION_ALIAS(__aeabi_f2d, __extendsfdf2)
DEFINE_AEABI_FUNCTION_ALIAS(__aeabi_d2iz, __fixdfsi)
DEFINE_AEABI_FUNCTION_ALIAS(__aeabi_f2iz, __fixsfsi)
DEFINE_AEABI_FUNCTION_ALIAS(__aeabi_d2uiz, __fixunsdfsi)
DEFINE_AEABI_FUNCTION_ALIAS(__aeabi_f2uiz, __fixunssfsi)
DEFINE_AEABI_FUNCTION_ALIAS(__aeabi_i2d, __floatsidf)
DEFINE_AEABI_FUNCTION_ALIAS(__aeabi_i2f, __floatsisf)
DEFINE_AEABI_FUNCTION_ALIAS(__aeabi_dmul, __muldf3)
DEFINE_AEABI_FUNCTION_ALIAS(__aeabi_fmul, __mulsf3)
DEFINE_AEABI_FUNCTION_ALIAS(__aeabi_dneg, __negdf2)
DEFINE_AEABI_FUNCTION_ALIAS(__aeabi_fneg, __negsf2)
DEFINE_AEABI_FUNCTION_ALIAS(__aeabi_dsub, __subdf3)
DEFINE_AEABI_FUNCTION_ALIAS(__aeabi_fsub, __subsf3)
DEFINE_AEABI_FUNCTION_ALIAS(__aeabi_d2f, __truncdfsf2)
DEFINE_AEABI_FUNCTION_ALIAS(__aeabi_dcmpun, __unorddf2)
DEFINE_AEABI_FUNCTION_ALIAS(__aeabi_fcmpun, __unordsf2)

// Alias __floatunsidf to __floatunssidfvfp
DEFINE_COMPILERRT_FUNCTION_ALIAS(__floatunsidf, __floatunssidfvfp)
DEFINE_AEABI_FUNCTION_ALIAS(__aeabi_ui2d, __floatunsidf)

// Alias __floatunsisf to __floatunsisfvfp
DEFINE_COMPILERRT_FUNCTION_ALIAS(__floatunsisf, __floatunssisfvfp)
DEFINE_AEABI_FUNCTION_ALIAS(__aeabi_ui2f, __floatunsisf)

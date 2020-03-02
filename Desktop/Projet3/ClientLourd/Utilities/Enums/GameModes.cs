using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ClientLourd.Utilities.Enums
{
    public enum GameModes
    {
        [Description("FFA")]
        FFA = 0,
        [Description("Solo")]
        Solo = 1,
        [Description("Coop")]
        Coop = 2,
    }
}

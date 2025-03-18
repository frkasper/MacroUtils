# -*- coding: utf-8 -*-
"""
Basic definitions for the demos.

@author: Fabio Kasper
"""
from pathlib import Path
from dataclasses import dataclass


class Case:

    @property
    def name(self) -> str:
        return self.__class__.__name__


@dataclass
class Macro(Case):
    macro_name: str

    @property
    def java_file(self) -> str:
        return f'{self.macro_name}.java'

    @property
    def name(self) -> str:
        return f'{self.macro_name}'

    def java_path(self, testhome: Path) -> Path:
        path = testhome.joinpath(self.java_file)
        if not path.is_file():
            raise OSError(f'not a file: {path!r}')
        return path


@dataclass
class Bug(Macro):
    load_demo: str


@dataclass
class Demo(Case):
    id: int                  # demo number
    np: int                  # core count
    batch: bool              # run in batch?
    files: list[str]         # list of support files

    @property
    def name(self) -> str:
        return f'{super().name}{self.id:02d}'

    def java_files(self, demohome: Path) -> list[Path]:
        """Return a list of JAVA files files related to this demo"""
        files = demohome.glob(f'Demo{self.id}_*.java')
        if not files:
            raise OSError(f'no JAVA files found for {self}')
        return list(files)

    def running_files(self, demohome: Path, datahome: Path) -> list[Path]:
        """Return a list of necessary files to run this demo"""
        paths = self.java_files(demohome)
        for file in self.files:
            path = datahome.joinpath(file)
            paths.append(path)
            if not path.is_file():
                raise OSError(f'invalid {path=}')
        return sorted(paths)


class SimAssistant(Macro):
    ...


class SimTool(Macro):
    ...


CASES = [  # A list defining all cases
    #-----------
    # Demos
    #-----------
    Demo(id=1, np=2, batch=True, files=[]),
    Demo(id=2, np=2, batch=True, files=[]),
    Demo(id=3, np=2, batch=True, files=[]),
    Demo(id=4, np=1, batch=True, files=['radial_impeller.stp']),
    Demo(id=5, np=6, batch=True, files=['LegoKart.x_b']),
    # Demo 6 is bypassed
    Demo(id=7, np=4, batch=True, files=[]),
    Demo(id=8, np=6, batch=True, files=['WING.x_b', 'wingCams.txt']),
    # Demo 9 is bypassed
    Demo(id=10, np=1, batch=True, files=[]),
    Demo(id=11, np=1, batch=True, files=[]),
    Demo(id=12, np=2, batch=True, files=[]),
    Demo(id=13, np=2, batch=True, files=[]),
    Demo(id=14, np=1, batch=True, files=[]),
    Demo(id=15, np=8, batch=True, files=['TableFromPeriodicRun.csv']),
    Demo(id=16, np=4, batch=True, files=[]),
    #-----------
    # Bugs
    #-----------
    Bug(load_demo=1, macro_name='BugCreateStreamlineSceneTest'),
    #-----------
    # Simulation assistants
    #-----------
    SimAssistant(macro_name='SimAssistantBlockMesherTest'),
    #-----------
    # Simulation tools
    #-----------
    SimTool(macro_name='SimToolImplicitUnsteadyConvergenceCheckerTest'),
    SimTool(macro_name='SimToolMeshMetricsTest'),
    ]


def filtered_cases(cases: list[Case], class_type: Case) -> list[Case]:
    return list(filter(lambda x: isinstance(x, class_type), cases))


def get_demo(number: int) -> Demo:
    demos = filtered_cases(CASES, Demo)
    ids = [demo.id for demo in demos]
    try:
        if int(number) in ids:
            return demos[ids.index(int(number))]
    except Exception:
        ... # Do nothing
    raise ValueError(f'invalid demo {number = }')


def main():
    """Local debug"""
    testhome = Path(__file__).parent.joinpath('macros')

    demo = get_demo(1)
    print(f'{demo=}')
    print(f'{demo.name=}')

    print(filtered_cases(CASES, Bug)[0].java_path(testhome))
    print(filtered_cases(CASES, SimAssistant)[0].name)


if __name__ == "__main__":
    main()

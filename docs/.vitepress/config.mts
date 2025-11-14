import {defineConfig} from 'vitepress'
import {withMermaid} from 'vitepress-plugin-mermaid'

const reportPath = '/report'
const implementationPath = `${reportPath}/implementation`
const processPath = `${reportPath}/process`

// https://vitepress.dev/reference/site-config
export default withMermaid(defineConfig({
    base: '/PPS-24-whodunnit/',
    title: "Whodunnit",
    description: "A investigative game",
    themeConfig: {
        // https://vitepress.dev/reference/default-theme-config
        nav: [
            {text: 'Home', link: '/'}
        ],

        sidebar: [
            {
                text: 'Report',
                items: [
                    { text: 'Introduction', link: `${reportPath}/0-introduction` },
                    { text: 'Development Process', link: `${reportPath}/1-development-process` },
                    { text: 'Requirements', link: `${reportPath}/2-requirements` },
                    { text: 'High Level Design', link: `${reportPath}/3-high-level-design` },
                    { text: 'Detailed Design', link: `${reportPath}/4-detailed-design` },
                    {
                        text: 'Implementation',
                        items: [
                            {
                                text: 'Lucia Castellucci',
                                items: [
                                    { text: 'Versioning System', link: `${implementationPath}/5-castellucci-versioning` },
                                    { text: 'Architecture', link: `${implementationPath}/5-castellucci-architecture`},
                                    { text: 'Controller', link: `${implementationPath}/5-castellucci-controller` },
                                    { text: 'View', link: `${implementationPath}/5-castellucci-view` },
                                ],
                                link: `${implementationPath}/5-castellucci`
                            },
                            {text: 'Roberto Mitugno', link: `${implementationPath}/5-mitugno`},
                            {
                                text: 'Luca Samorè',
                                items: [
                                    { text: 'Knowledge Graph', link: `${implementationPath}/5-samore-kg` },
                                    { text: 'Producer', link: `${implementationPath}/5-samore-producer` },
                                    { text: 'Rule DSL', link: `${implementationPath}/5-samore-dsl` },
                                ]
                            },
                        ]
                    },
                    { text: 'Testing', link: `${reportPath}/6-testing` },
                    { text: 'Retrospective', link: `${reportPath}/7-retrospective` },
                    {
                        text: 'Development Process Artifacts',
                        items: [
                            { text: 'Kickoff meeting', link: `${processPath}/8-kickoff-meeting` },
                            { text: 'Sprint 1', link: `${processPath}/8-sprint-1` },
                            { text: 'Sprint 2', link: `${processPath}/8-sprint-2` },
                            { text: 'Sprint 3', link: `${processPath}/8-sprint-3` },
                            { text: 'Sprint 4', link: `${processPath}/8-sprint-4` },
                            { text: 'Sprint 5', link: `${processPath}/8-sprint-5` },
                            { text: 'Sprint 6', link: `${processPath}/8-sprint-6` }
                        ]
                    }
                ]
            }
        ],

        socialLinks: [
            {icon: 'github', link: 'https://github.com/LuciaCastellucci/PPS-24-whodunnit'}
        ]
    }
}))
